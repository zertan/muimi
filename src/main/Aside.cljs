(ns main
  (:require ["@w3t-ab/sqeave" :as sqeave]
            ["solid-js" :refer [For createEffect createResource]]
            ["@solidjs/router" :refer [Route Router useNavigate useParams useLocation]]
            [clojure.string :as str])
  (:require-macros [sqeave :refer [defc]]))

(def color-map
  {:science "#66f6ff"
   :tech "#7cffb7"
   :politics "#ff9bd1"
   :philosophy "#c7a7ff"
   :business "#ffc26f"})

(defn post-path [{:post/keys [id]}]
  (str "/post/" id))

(defn pick-post [posts slug category]
  (let [filtered (if category
                   (filter #(= category (:post/category %)) posts)
                   posts)]
    (or (some #(when (= slug (:post/slug %)) %) filtered)
        (first filtered))))

;;(createEffect (fn [] (select-post! this (id) target)))

(defn hue [cat]
  (get color-map cat "#f2f2f2"))

(defc Aside [this {:blog/keys [id post category categories
                               {posts [:post/id :post/title :post/date :post/category :post/slug :post/markdown]}]}]
  #jsx
  [:aside {:class "pane pane-right pane-fixed" :style {:text-align "right"}}
   [:img {:style {:width "200px" :height "200px"}
          :src "./assets/muimi.png"}]
   [:div {:class "brand"}
    [:div {:class "brand-name"} "muimi"]
    [:div {:class "brand-caption"} "in the end, nothing matters"]]
   [:a {:class "about-link" :href "/about"} "About"]
   #_[:p {} "muimi is a minimal blog rendered with sqeave and Solid."]
   [:div {:class "menu-title"} "Categories"]
   [:div {:class "category-list"}
    [For {:each  (categories)}
     (fn [cat _]
       #jsx [:a {:key cat
                 :style {:color (hue cat)
                         :text-shadow (str "0 0 8px " (hue cat) "66")}
                 :class (str "category-chip " (when (= cat (:post/category (post))) "active"))
                 :href (str "/category/" cat)}
             cat])]]
   [:div {:class "menu-title"} "Posts"]
   [:div {:class "post-list"}
    [For {:each (posts)}
     (fn [p _]
       #jsx [:a {:key (:post/slug p)
                      :style {:color (hue (:post/category p))
                              :text-shadow (str "0 0 10px " (hue (:post/category p)) "66")
                              :text-align "right"}
                      :class (str "post-row " (when (= (:post/slug p) (:post/slug (post))) "active"))
                 :href (post-path p)}
             [:div {:class "post-row-title"} (:post/title p)]
             [:div {:class "post-row-meta"} (:post/date p)]])]]])
