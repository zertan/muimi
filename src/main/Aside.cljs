(ns main
  (:require ["solid-js" :refer [For]]
            ["@w3t-ab/sqeave" :as sqeave])
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
  [:aside {:class "sticky top-8 hidden h-fit space-y-6 text-right lg:block"}
   [:div {:class "rounded-2xl border border-slate-800/60 bg-slate-900/60 p-5 shadow-glow backdrop-blur"}
    [:div {:class "brand mb-3"}
     [:div {:class "text-sm uppercase tracking-[0.3em] text-slate-400"} "muimi"]
     [:div {:class "text-lg font-semibold text-white"} "in the end, nothing matters"]]
    [:a {:class "inline-flex items-center text-sm font-medium text-slate-200 transition hover:text-white"
         :href "/about"}
     "About"]]
   [:div {:class "rounded-2xl border border-slate-800/60 bg-slate-900/60 p-5 shadow-glow backdrop-blur"}
    [:div {:class "mb-3 text-[11px] uppercase tracking-[0.24em] text-slate-400"} "Categories"]
    [:div {:class "flex flex-col gap-2"}
     [For {:each  (categories)}
      (fn [cat _]
        #jsx [:a {:key cat
                  :style {:color (hue cat)}
                  :class (str "flex items-center justify-between rounded-lg border border-slate-800/80 px-3 py-2 text-sm text-slate-100 transition hover:border-slate-700 hover:bg-slate-800/60 " (when (= cat (:post/category (post))) "bg-slate-800/80 ring-1 ring-slate-700"))
                  :href (str "/category/" cat)}
              [:span {:class "capitalize"} cat]
              [:span {:class "text-[11px] uppercase tracking-[0.18em] text-slate-400"} "browse"]])]]]
   [:div {:class "rounded-2xl border border-slate-800/60 bg-slate-900/60 p-5 shadow-glow backdrop-blur"}
    [:div {:class "mb-3 text-[11px] uppercase tracking-[0.24em] text-slate-400"} "Posts"]
    [:div {:class "flex flex-col gap-2"}
     [For {:each (posts)}
      (fn [p _]
        #jsx [:a {:key (:post/slug p)
                 :style {:color (hue (:post/category p))}
                 :class (str "rounded-lg border border-slate-800/80 px-3 py-2 text-left text-slate-100 transition hover:border-slate-700 hover:bg-slate-800/60 " (when (= (:post/slug p) (:post/slug (post))) "bg-slate-800/80 ring-1 ring-slate-700"))
                 :href (post-path p)}
              [:div {:class "text-sm font-semibold leading-tight"} (:post/title p)]
              [:div {:class "text-[11px] uppercase tracking-[0.18em] text-slate-400"} (:post/date p)]])]]]])
