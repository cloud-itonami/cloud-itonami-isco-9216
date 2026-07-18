(ns fisheryaquaculture.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [fisheryaquaculture.actor :as actor]
            [fisheryaquaculture.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-worker! st {:worker-id "worker-1" :name "Aya Fujimoto"})
    (store/register-site! st {:site-id "SITE-1" :name "Fujimoto Aquaculture Site" :max-supply-cost 2000})
    st))

(deftest commits-a-registered-work-log
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:worker-id "worker-1" :op :log-work-record :stake :low
                  :site-id "SITE-1" :task "harvest progress log"}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "worker-1"))))))

(deftest holds-an-unregistered-site-proposal
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:worker-id "worker-1" :op :log-work-record :stake :low
                  :site-id "SITE-ghost" :task "harvest progress log"}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :hold (:disposition (:state result))))
    (is (empty? (store/records-of st "worker-1")))))

(deftest interrupts-then-approves-safety-concern-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:worker-id "worker-1" :op :flag-safety-concern :stake :low
                  :site-id "SITE-1" :hazard-type :drowning-risk}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "worker-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (= 1 (count (store/records-of st "worker-1")))))))

(deftest holds-a-scope-excluded-work-execution-op-even-at-high-confidence
  (testing "an actor run can never commit a proposal that would finalize a fishery/aquaculture-work-execution decision, regardless of disposition path"
    (let [st (fresh-store)
          graph (actor/build-graph {:store st})
          request {:worker-id "worker-1" :op :authorize-water-based-operation :stake :low
                    :site-id "SITE-1" :task "water-based operation authorization"}
          result (actor/run-request! graph request {} "thread-4")]
      (is (= :done (:status result)))
      (is (= :hold (:disposition (:state result))))
      (is (empty? (store/records-of st "worker-1"))))))

(deftest holds-a-scope-excluded-site-safety-clearance-op-even-at-high-confidence
  (testing "an actor run can never commit a proposal that would finalize a site-safety-clearance decision (e.g. declaring a site cleared for safety), regardless of disposition path"
    (let [st (fresh-store)
          graph (actor/build-graph {:store st})
          request {:worker-id "worker-1" :op :declare-site-safety-cleared :stake :low
                    :site-id "SITE-1" :task "site safety clearance"}
          result (actor/run-request! graph request {} "thread-5")]
      (is (= :done (:status result)))
      (is (= :hold (:disposition (:state result))))
      (is (empty? (store/records-of st "worker-1"))))))
