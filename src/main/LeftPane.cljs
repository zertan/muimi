(ns main
  (:require ["@w3t-ab/sqeave" :as sqeave]
            ["solid-js" :refer [For createEffect createResource Show]]
            ["@solidjs/router" :refer [Route Router useNavigate useParams useLocation A]]
            [clojure.string :as str])
  (:require-macros [sqeave :refer [defc]]))

(defc LeftPane [this {:keys [toc]}]
  (let [location (useLocation)]
    #jsx [:aside {:class "pane pane-left pane-fixed"}
          [Show {:when (contains? location "post")}
           [:div {:class "menu-title"} "TOC"]
           [For {:each (or toc [])}
            (fn [{:keys [id title]} _]
              #jsx [:div {:class "toc-row" :key id}
                    [:a {:href (str "#" id)}
                     title]])]]]))
