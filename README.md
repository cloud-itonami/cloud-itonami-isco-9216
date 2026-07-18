# cloud-itonami-isco-9216

Open Occupation Blueprint for **ISCO-08 9216**: Fishery and Aquaculture
Labourers.

This repository designs a forkable OSS business for a fishery/aquaculture-
site scheduling and logistics coordination practice: a site scheduling and
supply-coordination robot manages crew/task records under a governor-gated
actor, so a fishery or aquaculture crew keeps its own operating records
instead of renting a closed workforce-management SaaS.

**Maturity: `:implemented`.** `src/fisheryaquaculture/` implements the
`FisheryAquacultureActor` as a `langgraph.graph/state-graph`
(`fisheryaquaculture.actor`) wired to a `Fishery and Aquaculture Labourer
Advisor` (`fisheryaquaculture.advisor`) and an independent
`FisheryAquacultureGovernor` (`fisheryaquaculture.governor`), following the
itonami actor pattern (ADR-2607121000): `:intake -> :advise -> :govern ->
:decide -+-> :commit (:ok?) +-> :request-approval (:escalate?,
human-in-the-loop interrupt) +-> :hold (:hard?)`. 24 tests / 52 assertions
green (`clojure -M:test`). HARD invariants (always hold, never
overridable): worker provenance, site provenance, no-actuation (`:effect`
must be `:propose`), a closed op-allowlist (`:log-work-record`,
`:schedule-crew-operation`, `:flag-safety-concern`,
`:coordinate-supply-order` — nothing else may ever be proposed), and a
permanent, unconditional block on any proposal that would directly
finalize a fishery/aquaculture-work-execution decision (e.g. authorizing a
water-based operation to proceed) *or* a site-safety-clearance decision
(e.g. declaring a site cleared for safety), or that would override a site
safety supervisor's judgment. Always-escalate paths (human sign-off
regardless of confidence, mapping this repo's Trust Controls in
[`docs/business-model.md`](docs/business-model.md)): `:flag-safety-concern`
(always, no confidence-threshold exception) and `:coordinate-supply-order`
above the registered cost threshold.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot
performs the physical domain work**. Here a site scheduling/logistics
coordination robot performs crew scheduling, harvest/task/progress-record
logging and feed/equipment procurement coordination for a fishery or
aquaculture crew, under an actor that proposes actions and an independent
**FisheryAquacultureGovernor** that gates them. The governor never
dispatches hardware itself, never performs fishery or aquaculture work on
the site itself, and never finalizes a fishery/aquaculture-work-execution
decision or a site-safety-clearance decision, and never overrides a site
safety supervisor's judgment; `:high`/`:safety-critical` actions (such as a
flagged drowning-risk/aquatic-environment/equipment-condition/weather
concern, or an above-threshold supply order) require human sign-off. **This
actor coordinates SITE SCHEDULING/LOGISTICS ONLY — it never performs
fishery or aquaculture work itself and never makes a site-safety-clearance
decision itself.**

## Core Contract

```text
worker roster + site registration + safety-reporting policy
        |
        v
Fishery and Aquaculture Labourer Advisor -> FisheryAquacultureGovernor -> log/schedule/coordinate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses,
finalize a fishery/aquaculture-work-execution decision, finalize a
site-safety-clearance decision (e.g. declaring a site cleared for safety),
override a site safety supervisor's judgment, suppress an operating
record, or disclose sensitive data without governor approval and audit
evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `9216`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
