(ns main
  (:require ["@w3t-ab/sqeave" :as sqeave]
            ["solid-js" :refer [For createEffect createResource onMount createMemo children]]
            ["@solidjs/router" :refer [Route Router useNavigate useParams useLocation]]
            ["./Post.cljs" :refer [Post]]
            ["./db.cljs" :as db]
            ["./md.cljs" :as md]
            ["./Aside.cljs" :refer [Aside]]
            ["./LeftPane.cljs" :refer [LeftPane]]
            [clojure.string :as str])
  (:require-macros [sqeave :refer [defc]]))

#_(defn slugify [text seen]
  (let [base (-> text
                 str/lower-case
                 (str/replace #"[^a-z0-9]+" "-")
                 (str/replace #"^-+|-+$" ""))]
    (loop [candidate base idx 1]
      (if (contains? @seen candidate)
        (recur (str base "-" idx) (inc idx))
        (do (swap! seen conj candidate)
            candidate)))))

(defc Category [this {:keys/category [id]}]
  #jsx [:div {} "Category"])

(defc About [this {:keys/about [id description]}]
  #jsx [:div {} "About"])

(defn Subscribe []
  #jsx [:div {:class "subscribe"}
        [:h2 {} "Subscribe"]
        [:p {} "Drop an email to stay in the loop."]
        [:form {:class "subscribe-form" :action "mailto:subscribe@muimi.local" :method "post"}
         [:input {:type "email" :name "email" :placeholder "you@example.com" :required true}]
         [:button {:type "submit"} "Send"]]])

(defc Blog [this {:blog/keys [id]}]
  #jsx [:div {:class "layout"}
        [LeftPane {:toc (:toc {})}]
        [:main {:class "pane pane-main"}
         props.children]
        [Aside {:ident [:blog/id (id)]}]
        [Subscribe]])

(defc Main [this {:blog/keys [id posts post categories]
                  :or {id 1
                       post [:post/id (:post/id (first db/posts))]
                       categories db/categories
                       posts db/posts}}]
  (do
    (onMount md/shiki-md-init)
    #jsx [Router {:root (fn [props]
                          #jsx [Blog {:& (merge props {:ident [:blog/id (id)]})}])}
          [Route {:path "/about"
                  :component (fn [props2]
                               #jsx [About {:ident [:about/id (id)]}])}]
          [Route {:path "/post/:id"
                  :component (fn [props2]
                               (let [params (useParams)]
                                 #jsx [Post {:& {:ident [:post/id (:id params)]}}]))}]
          [Route {:path "/category/:id"
                  :component (fn [_]
                               (let [params (useParams)]
                                 #jsx [Category {:ident [:category/id (:id params)]}]))}]]))
