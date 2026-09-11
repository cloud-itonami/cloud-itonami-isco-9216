(ns fisheryaquaculture.store
  "SSoT for the ISCO-08 9216 fishery/aquaculture-site scheduling/logistics
  coordination actor (itonami actor pattern, ADR-2607121000 / CLAUDE.md
  Actors section; README's 'Robotics premise' — a site scheduling/logistics
  coordination robot manages crew/task records, harvest/progress logging
  and feed/equipment procurement coordination for a fishery or aquaculture
  site crew under this advisor/governor pair, which never dispatches
  hardware itself, never performs fishery/aquaculture work itself, and
  never finalizes a fishery/aquaculture-work-execution decision or a
  site-safety-clearance decision, and never overrides a site safety
  supervisor's judgment — those remain the site safety supervisor's
  exclusive judgment). Modeled closely on cloud-itonami-isco-9212's
  livestockfarm.store for the elementary-occupation outdoor-labour
  hazard-domain shape, extended with an independent
  drowning-risk/aquatic-environment-exposure hazard-scope dimension
  (fishery and aquaculture labourers work on or near water, so drowning
  risk and aquatic-environment/outdoor weather-terrain exposure stack on
  top of the general elementary-occupation manual-labour hazard).

  Domain:

    worker — a registered fishery/aquaculture-site crew member
             (:worker-id, :name)
    site   — a registered fishery or aquaculture site {:site-id :name
             :max-supply-cost number}. `:max-supply-cost` is an
             informational registered ceiling used only to decide whether
             a `:coordinate-supply-order` proposal escalates to human
             sign-off (the governor never blocks a within-threshold order
             outright; it only decides commit vs. escalate).
    record — a committed operating record (a logged harvest/task/progress
             entry, a scheduled crew operation, a flagged safety concern,
             or a coordinated supply order) — written ONLY via
             commit-record!.
    ledger — append-only audit trail, commit or hold.")

(defprotocol Store
  (worker [s worker-id])
  (site [s site-id])
  (records-of [s worker-id])
  (ledger [s])
  (register-worker! [s worker])
  (register-site! [s site])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (worker [_ worker-id] (get-in @a [:workers worker-id]))
  (site [_ site-id] (get-in @a [:sites site-id]))
  (records-of [_ worker-id] (filter #(= worker-id (:worker-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-worker! [s w]
    (swap! a assoc-in [:workers (:worker-id w)] w) s)
  (register-site! [s f]
    (swap! a assoc-in [:sites (:site-id f)] f) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:workers {} :sites {} :records [] :ledger []}
                                    seed)))))
