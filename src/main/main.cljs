(ns main
  (:require ["@w3t-ab/sqeave" :as sqeave]
            ["solid-js" :refer [For createEffect createResource]]
            ["@solidjs/router" :refer [Route Routes useNavigate]]
            [clojure.string :as str])
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

(def color-map
  {:science "#66f6ff"
   :tech "#7cffb7"
   :politics "#ff9bd1"
   :philosophy "#c7a7ff"
   :business "#ffc26f"})

(defn hue [cat]
  (get color-map cat "#f2f2f2"))

(defn post-path [{:post/keys [slug category]}]
  (str "/" (name category) "/" slug))

(defn pick-post [posts slug category]
  (let [filtered (if category
                   (filter #(= category (:post/category %)) posts)
                   posts)]
    (or (some #(when (= slug (:post/slug %)) %) filtered)
        (first filtered))))

(defn fetch-markdown [path]
  (when path
    (-> (js/fetch path)
        (.then (fn [resp] (.text resp))))))

(defn slugify [text seen]
  (let [base (-> text
                 str/lower-case
                 (str/replace #"[^a-z0-9]+" "-")
                 (str/replace #"^-+|-+$" ""))]
    (loop [candidate base idx 1]
      (if (contains? @seen candidate)
        (recur (str base "-" idx) (inc idx))
        (do (swap! seen conj candidate)
            candidate)))))

(defn load-markdown-renderer []
  (-> (js/Promise.all #js [(js/import "markdown-it") (js/import "shiki")])
      (.then (fn [[md-lib shiki]]
               (-> (.getHighlighter (.-default shiki) (clj->js {:theme "poimandres"}))
                   (.then (fn [highlighter]
                            (let [md (new (.-default md-lib)
                                          (clj->js {:highlight (fn [code lang]
                                                                 (.codeToHtml highlighter code
                                                                              (clj->js {:lang (or lang "text")
                                                                                        :theme "poimandres"})))
                                                    :html true}))
                                  seen (atom #{})
                                  heading-rule (fn [tokens idx options env self]
                                                 (let [content (.-content (aget tokens (inc idx)))
                                                       slug (slugify content seen)]
                                                   (.attrSet (aget tokens idx) "id" slug)
                                                   (.renderToken self tokens idx options)))]
                              (aset (.-renderer md) "rules" "heading_open" heading-rule)
                              (fn [markdown]
                                (let [env (js-obj)
                                      tokens (.parse md markdown env)
                                      toc (->> tokens
                                               (keep-indexed (fn [i token]
                                                               (when (= "heading_open" (.-type token))
                                                                 (let [heading-token (aget tokens (inc i))
                                                                       title (.-content heading-token)
                                                                       slug (.attrGet token "id")
                                                                       level (js/parseInt (subs (.-tag token) 1))]
                                                                   {:id slug :title title :level level}))))
                                               (filter #(<= (:level %) 3)))]
                                  {:html (.render md markdown env)
                                   :toc toc}))))))))))

(defn select-post! [this blog-id post]
  (when-let [pid (:post/id post)]
    (sqeave/add-ident! this [:post/id pid] {:replace [:blog/id blog-id :blog/post]})
    (sqeave/set! this :blog/category (:post/category post))))

(defc MarkdownBlock [this {:keys [rendered]}]
  (let [result (rendered)]
    #jsx [:div {:class "markdown-body"
                :innerHTML (or (:html result)
                               "<p>Loading markdown...</p>")}]))

(defc Aside [this {:blog/keys [id {posts [:post/id :post/title :post/date :post/category :post/slug :post/markdown]} post category]
                   :keys [navigate]}]
  (let [all-posts (posts)
        current (post)]
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
         (let [target (pick-post all-posts nil cat)]
           #jsx [:button {:key cat
                          :style {:color (hue cat) :text-shadow (str "0 0 8px " (hue cat) "66")}
                          :class (str "category-chip " (when (= cat (:post/category current)) "active"))
                          :onClick #(when target
                                      (select-post! this (id) target)
                                      (when navigate
                                        (navigate (post-path target))))}
                 cat]))]]
     [:div {:class "menu-title"} "Posts"]
     [:div {:class "post-list"}
      [For {:each all-posts}
       (fn [p _]
         #jsx [:button {:key (:post/slug p)
                        :style {:color (hue (:post/category p)) :text-shadow (str "0 0 10px " (hue (:post/category p)) "66")}
                        :class (str "post-row " (when (= (:post/slug p) (:post/slug current)) "active"))
                        :onClick #(do
                                    (select-post! this (id) p)
                                    (when navigate
                                      (navigate (post-path p))))}
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

(defc Post [this {:blog/keys [post] :keys [rendered]}]
  (let [current (post)
        result (rendered)]
    #jsx
    [:<>
     [:header {:class "hero"}
      [:div {:class "eyebrow"} (str (:post/category current) " • " (:post/date current))]
      [:h1 {:class "title"} (:post/title current)]
      [:p {:class "subtitle"} (:post/summary current)]
      [:div {:class "tag-row"}
       [For {:each (:post/tags current)}
        (fn [t _]
          #jsx [:span {:class "tag" :key t} t])]]]
     [MarkdownBlock {:rendered rendered}]]))

(defc LeftPane [this {:keys [toc]}]
  #jsx [:aside {:class "pane pane-left pane-fixed"}
        [:div {:class "menu-title"} "TOC"]
        [For {:each (or toc [])}
         (fn [{:keys [id title]} _]
           #jsx [:div {:class "toc-row" :key id}
                 [:a {:href (str "#" id)}
                  title]])]])

(defc RoutedLayout [this {:blog/keys [id posts post category]
                          :route/keys [category-param slug-param]}]
  (let [navigate (useNavigate)]
    (createEffect
     (fn []
       (let [all (or (posts) [])
             cat-kw (some-> category-param keyword)
             target (or (pick-post all slug-param cat-kw)
                        (first all))]
         (when target
           (select-post! this (id) target)
           (when (and slug-param (not= slug-param (:post/slug target)))
             (navigate (post-path target) {:replace true})))))))
  (let [[markdown _] (createResource (fn [] (:post/markdown (post))) fetch-markdown)
        [rendered _] (createResource markdown (fn [md]
                                                (when md
                                                  (-> (load-markdown-renderer)
                                                      (.then (fn [render] (render md)))))))]
    #jsx [:div {:class "layout"}
          [LeftPane {:toc (:toc (or (rendered) {}))}]
          [:main {:class "pane pane-main"}
           [Post {:blog/post post
                  :rendered rendered}]]

          [Aside {:ident [:blog/id (id)]
                  :blog/posts posts
                  :blog/post post
                  :blog/category category
                  :navigate navigate}]]))

(defc Main [this {:blog/keys [id posts]
                  :or {id 1
                       posts sample-posts}}]
  #jsx [Routes {}
        [Route {:path "/"
                :component (fn [_]
                             #jsx [RoutedLayout {:ident [:blog/id (id)]
                                                 :blog/posts posts}])}]
        [Route {:path "/:category"
                :component (fn [props]
                             (let [params (.-params props)]
                               #jsx [RoutedLayout {:ident [:blog/id (id)]
                                                   :blog/posts posts
                                                   :route/category-param (aget params "category")}]))}]
        [Route {:path "/:category/:slug"
                :component (fn [props]
                             (let [params (.-params props)]
                               #jsx [RoutedLayout {:ident [:blog/id (id)]
                                                   :blog/posts posts
                                                   :route/category-param (aget params "category")
                                                   :route/slug-param (aget params "slug")}]))}]])
