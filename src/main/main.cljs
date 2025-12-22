(ns main
  (:require ["@w3t-ab/sqeave" :as sqeave]
            ["solid-js" :refer [For createEffect createMemo createResource]]
            ["@solidjs/router" :refer [Route Routes useNavigate useParams]])
  (:require-macros [sqeave :refer [defc]]))

(def categories [:science :tech :politics :philosophy :business])

(def sample-posts
  [{:post/id 1
    :post/slug "edge-pipelines"
    :post/title "Edge pipelines without the drama"
    :post/summary "Lightweight playbook for deploying tiny services to noisy edges."
    :post/category :tech
    :post/tags ["edge" "streaming" "ops"]
    :post/date "2024-12-02"
    :post/sections [{:title "Minimalist edge deployments"
                     :body "Shipping to the edge is mostly about removing surprises. Keep binaries slim and logging honest."}
                    {:title "Build profile"
                     :body "- Prefer static assets and zero-dependency binaries.\n- Turn on health probes that are legible to humans.\n- Document the fallback path when the network flakes."}
                    {:title "Observability trail"
                     :body "The only good alert is the one that points to a log line and a fix. Everything else is panic noise."}]}
   {:post/id 2
    :post/slug "quantum-systems"
    :post/title "Quantum systems that feel like code"
    :post/summary "Notes on wiring lab-grade measurements with programmer-minded tooling."
    :post/category :science
    :post/tags ["quantum" "experiments" "signal"]
    :post/date "2024-09-19"
    :post/sections [{:title "Quantum signal scaffolding"
                     :body "The lab notebook is turning into a code repo. Every measurement is a commit, and every waveform is a diff."}
                    {:title "Interfaces over instruments"
                     :body "- Wrap oscilloscopes with tiny adapters.\n- Move configuration into readable data files.\n- Keep the feedback loop short; graphs should recompile as fast as code."}
                    {:title "Field notes"
                     :body "When the laser drifts, the log should tell you which script changed. Traceability beats heroics."}]}
   {:post/id 3
    :post/slug "digital-commons"
    :post/title "Digital commons and the civic stack"
    :post/summary "A civic-ops perspective on governing shared digital infrastructure."
    :post/category :politics
    :post/tags ["policy" "internet" "civic-tech"]
    :post/date "2024-07-14"
    :post/sections [{:title "Civic-grade infrastructure"
                     :body "Public systems should read like clean source files—transparent and testable."}
                    {:title "Procurement as code review"
                     :body "- Ship RFPs with reproducible test cases.\n- Reward vendors that keep interfaces boring."}
                    {:title "Trust budgets"
                     :body "The trust budget drops every time the interface surprises the public. Ship predictable defaults."}]}
   {:post/id 4
    :post/slug "ethics-automation"
    :post/title "Automation with a conscience"
    :post/summary "Notes on building agents that respect human boundaries."
    :post/category :philosophy
    :post/tags ["ethics" "automation" "design"]
    :post/date "2024-10-05"
    :post/sections [{:title "Guardrails are features"
                     :body "Every automation pathway should declare what it will not do."}
                    {:title "Design prompts"
                     :body "- Write failure modes next to success cases.\n- Make the exit ramps obvious and reversible."}
                    {:title "Stewardship beats speed"
                     :body "A slower rollout with transparent logging builds more trust than a fast opaque launch."}]}
   {:post/id 5
    :post/slug "ops-capital"
    :post/title "Operational capital for builders"
    :post/summary "A minimalist toolkit for measuring and extending execution runway."
    :post/category :business
    :post/tags ["ops" "runway" "cadence"]
    :post/date "2024-11-18"
    :post/sections [{:title "Cadence as currency"
                     :body "Teams trade on predictability. A clean weekly cadence is worth more than a flashy roadmap."}
                    {:title "Signals to watch"
                     :body "- Deployment health without babysitting.\n- Burn plotted next to roadmap risk."}
                    {:title "Decision hygiene"
                     :body "If a decision cannot be explained in a paragraph, it is not ready."}]}
   ])

(def color-map
  {:science "#66f6ff"
   :tech "#7cffb7"
   :politics "#ff9bd1"
   :philosophy "#c7a7ff"
   :business "#ffc26f"})

(defn hue [cat]
  (get color-map cat "#f2f2f2"))

(defn pick-post [posts slug category]
  (let [filtered (if category
                   (filter #(= category (:post/category %)) posts)
                   posts)
        target (some #(when (= slug (:post/slug %)) %) filtered)]
    (or target (first filtered))))

(defn post-path [{:post/keys [slug category]}]
  (str "/" (name category) "/" slug))

(def remote-markdown-imports
  #js ["https://esm.sh/markdown-it@14.1.0?bundle"
       "https://esm.sh/shiki@1.23.0?bundle"])

(defn load-markdown-renderer []
  (-> (js/Promise.all remote-markdown-imports)
      (.then (fn [[md-lib shiki]]
               (-> (.getHighlighter (.-default shiki)
                                    (clj->js {:theme "poimandres"}))
                   (.then (fn [highlighter]
                            (let [md (new (.-default md-lib)
                                          (clj->js {:highlight (fn [code lang]
                                                                 (.codeToHtml highlighter code
                                                                              (clj->js {:lang (or lang "text")
                                                                                        :theme "poimandres"})))
                                                    :html true}))]
                              (fn [markdown]
                                (.render md (or markdown ""))))))))))

(defc MarkdownBlock [this {:keys [content]}]
  (let [[renderer _] (createResource load-markdown-renderer)
        rendered (createMemo #(when-let [f (renderer)]
                                (f content)))]
    #jsx [:div {:class "markdown-body"
                :innerHTML (or (rendered)
                               (str "<p>Loading renderer from remote edge..." "</p>"))}]))

 (defc Aside [this {:blog/keys [id {posts [:post/id :post/title :post/date :post/category :post/slug]}]
                    :keys [navigate current-category current-slug]}]
  (let [current-category (or current-category (fn [] nil))
        current-slug (or current-slug (fn [] nil))]
    #jsx
    [:aside {:class "pane pane-right pane-fixed"}
     [:img {:style {:width "200px" :height "200px"}
            :src "./assets/muimi.png"}]
     [:div {:class "brand"}
      [:div {:class "brand-name"} "muimi"]
      [:div {:class "brand-caption"} "coder noir journal"]]
     [:a {:class "about-link" :href "#about"} "About"]
     [:div {:class "menu-title"} "Categories"]
     [:div {:class "category-list"}
      [For {:each  categories}
       (fn [cat _]
         (let [first-slug (:post/slug (pick-post (posts) nil cat))]
           #jsx [:button {:key cat
                          :style {:color (hue cat) :text-shadow (str "0 0 8px " (hue cat) "66")}
                          :class (str "category-chip " (when (= cat (current-category)) "active"))
                          :onClick #(when navigate
                                      (navigate (str "/" (name cat) "/" first-slug)))}
                 cat]))]]
     [:div {:class "menu-title"} "Posts"]
     [:div {:class "post-list"}
      [For {:each (posts)}
       (fn [p _]
         #jsx [:button {:key (:post/slug p)
                        :style {:color (hue (:post/category p)) :text-shadow (str "0 0 10px " (hue (:post/category p)) "66")}
                        :class (str "post-row " (when (= (:post/slug p) (current-slug)) "active"))
                        :onClick #(when navigate
                                    (navigate (post-path p)))}
                [:div {:class "post-row-title"} (:post/title p)]
                [:div {:class "post-row-meta"} (:post/date p)]])]]
     [:section {:id "about" :class "about-block"}
      [:div {:class "menu-title"} "About"]
      [:p {} "muimi is a minimal blog rendered with sqeave and Solid."]]
     [:div {:class "subscribe"}
      [:h2 {} "Subscribe"]
      [:p {} "Drop an email to stay in the loop."]
      [:form {:class "subscribe-form" :action "mailto:subscribe@muimi.local" :method "post"}
       [:input {:type "email" :name "email" :placeholder "you@example.com" :required true}]
       [:button {:type "submit"} "Send"]]]]))

 (defc Post [this {:post/keys [id slug title summary category tags date sections]}]
  #jsx
  [:<>
   [:header {:class "hero"}
    [:div {:class "eyebrow"} (str (category) " • " (date))]
    [:h1 {:class "title"} (title)]
    [:p {:class "subtitle"} (summary)]
    [:div {:class "tag-row"}
     [For {:each (tags)}
      (fn [t _]
        #jsx [:span {:class "tag" :key t} t])]]]
   [For {:each (sections)}
    (fn [{:keys [title body]}]
      #jsx [:section {}
            [:h2 {:id title} title]
            [MarkdownBlock {:content body}]])]])

(defc LeftPane [this {:post/keys [sections title]}]
  #jsx [:aside {:class "pane pane-left pane-fixed"}
        [:div {:class "menu-title"} "TOC"]
        [For {:each (or sections [])}
         (fn [{:keys [title]} _]
           #jsx [:div {:class "toc-row" :key title}
                 [:a {:href (str "#" title)}
                  title]])]])

(defc RoutedLayout [this {:blog/keys [id {posts [:post/id :post/title :post/date :post/category :post/slug]}]
                         :or {id 1
                              posts sample-posts}}]
  (let [navigate (useNavigate)
        params (useParams)
        category-param (createMemo #(some-> (aget params "category") keyword))
        slug-param (createMemo #(aget params "slug"))
        active-post (createMemo #(pick-post (posts) (slug-param) (category-param)))
        active-category (createMemo #(or (category-param)
                                         (:post/category (active-post))))
        active-slug (createMemo #(:post/slug (active-post)))]
    (createEffect
     (fn []
       (when (and (active-post) (not (slug-param)))
         (navigate (post-path (active-post)) {:replace true}))))
    #jsx [:div {:class "layout"}
          [LeftPane {:post/sections (:post/sections (active-post))
                     :post/title (:post/title (active-post))}]
          [:main {:class "pane pane-main"}
           [Post {:post/id (:post/id (active-post))
                  :post/slug (:post/slug (active-post))
                  :post/title (:post/title (active-post))
                  :post/summary (:post/summary (active-post))
                  :post/category (:post/category (active-post))
                  :post/tags (:post/tags (active-post))
                  :post/date (:post/date (active-post))
                  :post/sections (:post/sections (active-post))}]]

          [Aside {:ident [:blog/id (id)]
                  :navigate navigate
                  :current-category active-category
                  :current-slug active-slug}]]))

(defc Main [this {:blog/keys [id {posts [:post/id :post/title]}]
                  :or {id 1
                       posts sample-posts}}]
  #jsx [Routes {}
        [Route {:path "/" :element (#jsx [RoutedLayout {:ident [:blog/id (id)] :blog/posts posts}])}]
        [Route {:path "/:category" :element (#jsx [RoutedLayout {:ident [:blog/id (id)] :blog/posts posts}])}]
        [Route {:path "/:category/:slug" :element (#jsx [RoutedLayout {:ident [:blog/id (id)] :blog/posts posts}])}]])
