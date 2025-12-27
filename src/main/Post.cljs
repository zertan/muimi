(ns post
  (:require ["solid-js" :refer [For]]
            ["./md.cljs" :as md]
            ["@w3t-ab/sqeave" :as sqeave])
  (:require-macros [sqeave :refer [defc]]))

(defc Post [this {:post/keys [id slug title summary tags markdown date category]}]
  #jsx
  [:<>
   [:header {:class "rounded-2xl border border-slate-800/60 bg-gradient-to-br from-slate-900 via-slate-900/70 to-slate-800 p-6 shadow-glow"}
    [:div {:class "text-xs uppercase tracking-[0.3em] text-slate-400"} (str (category) " • " (date))]
    [:h1 {:class "mt-3 text-3xl font-semibold leading-tight text-white"} (title)]
    [:div {:class "tag-row mt-3 flex flex-wrap gap-2"}
     [For {:each (tags)}
      (fn [t _]
        #jsx [:span {:class "inline-flex items-center rounded-full bg-slate-800/70 px-3 py-1 text-xs font-semibold text-slate-100 shadow-inner shadow-black/30" :key t} t])]]
    [:p {:class "mt-3 text-base leading-relaxed text-slate-300"} (summary)]]
   [md/Markdown {:source markdown}]])
