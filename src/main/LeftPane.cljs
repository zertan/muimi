(ns main
  (:require ["solid-js" :refer [For Show createMemo]]
            ["@solidjs/router" :refer [useLocation]]
            ["./Subscribe.cljs" :refer [SubscribeCard]]
            [clojure.string :as str])
  (:require-macros [sqeave :refer [defc]]))

(defn- post-page? [location]
  (let [pathname (.-pathname location)]
    (and (string? pathname)
         (or (str/starts-with? pathname "/post/")
             (= pathname "/")))))

(defc LeftPane [this {:keys [toc]}]
  (let [location (useLocation)
        show-toc? (createMemo #(and (seq toc) (post-page? location)))]
    #jsx [:aside {:class "sticky top-8 flex h-fit flex-col gap-6"}
          [SubscribeCard]
          [Show {:when (show-toc?)}
           [:div {:class "rounded-2xl border border-slate-800/60 bg-slate-900/60 p-5 shadow-glow backdrop-blur"}
            [:div {:class "mb-3 text-[11px] uppercase tracking-[0.24em] text-slate-400"} "On this page"]
            [:div {:class "space-y-2"}
             [For {:each (or toc [])}
              (fn [{:keys [id title]} _]
                #jsx [:div {:key id}
                      [:a {:href (str "#" id)
                           :class "block rounded-md px-2 py-1 text-sm text-slate-200 transition hover:bg-slate-800/60 hover:text-white"}
                       title]])]]]]]))
