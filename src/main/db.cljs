(ns db)

(def categories [:science :tech :politics :philosophy :business])

(def posts
  [{:post/id 1
    :post/slug "edge-pipelines"
    :post/title "Edge pipelines without the drama"
    :post/summary "Lightweight playbook for deploying tiny services to noisy edges."
    :post/category :tech
    :post/tags ["edge" "streaming" "ops"]
    :post/date "2024-12-02"
    :post/markdown "/posts/edge-pipelines.md"}
   {:post/id 2
    :post/slug "quantum-systems"
    :post/title "Quantum systems that feel like code"
    :post/summary "Notes on wiring lab-grade measurements with programmer-minded tooling."
    :post/category :science
    :post/tags ["quantum" "experiments" "signal"]
    :post/date "2024-09-19"
    :post/markdown "/posts/quantum-systems.md"}
   {:post/id 3
    :post/slug "digital-commons"
    :post/title "Digital commons and the civic stack"
    :post/summary "A civic-ops perspective on governing shared digital infrastructure."
    :post/category :politics
    :post/tags ["policy" "internet" "civic-tech"]
    :post/date "2024-07-14"
    :post/markdown "/posts/digital-commons.md"}
   {:post/id 4
    :post/slug "ethics-automation"
    :post/title "Automation with a conscience"
    :post/summary "Notes on building agents that respect human boundaries."
    :post/category :philosophy
    :post/tags ["ethics" "automation" "design"]
    :post/date "2024-10-05"
    :post/markdown "/posts/ethics-automation.md"}
   {:post/id 5
    :post/slug "ops-capital"
    :post/title "Operational capital for builders"
    :post/summary "A minimalist toolkit for measuring and extending execution runway."
    :post/category :business
    :post/tags ["ops" "runway" "cadence"]
    :post/date "2024-11-18"
    :post/markdown "/posts/ops-capital.md"}])
