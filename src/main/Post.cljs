(ns post
  (:require ["@w3t-ab/sqeave" :as sqeave]
            ["solid-js" :refer [For createEffect createResource createMemo]]
            ["@solidjs/router" :refer [Route Router useNavigate useParams useLocation]]
            ["./md.cljs" :as md]
            ["./db.cljs" :as db]
            [clojure.string :as str])
  (:require-macros [sqeave :refer [defc]]))

#_(defn select-post! [this blog-id post]
  (when-let [pid (:post/id post)]
    (sqeave/add-ident! this [:post/id pid] {:replace [:blog/id blog-id :blog/post]})
    (sqeave/set! this :blog/category (:post/category post))))

(defn fetch-markdown [path]
  (when path
    (js/console.log "fetch:" path)
    (-> (js/fetch path)
        (.then (fn [resp] (.text resp))))))

(defc Post [this {:post/keys [id slug title summary tags markdown date category]}]
  #jsx
  [:<>
   [:header {:class "hero"}
    [:div {:class "eyebrow"} (str (category) " • " (date))]
    [:h1 {:class "title"} (title)]
    [:div {:class "tag-row"}
     [For {:each (tags)}
      (fn [t _]
        #jsx [:span {:class "tag" :key t} t])]]
    [:p {:class "subtitle"} (summary)]]
   (markdown)
   [md/Markdown {:source markdown}]])
