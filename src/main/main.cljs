(ns main
  (:require ["@w3t-ab/sqeave" :as sqeave]
            ["solid-js" :refer [onMount]]
            ["@solidjs/router" :refer [Route Router useParams]]
            ["./Post.cljs" :refer [Post]]
            ["./db.cljs" :as db]
            ["./md.cljs" :as md]
            ["./Aside.cljs" :refer [Aside]]
            ["./LeftPane.cljs" :refer [LeftPane]])
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

(defc Blog [this {:blog/keys [id]}]
  #jsx
  [:div {:class "bg-slate-950"}
   [:div {:class "mx-auto flex min-h-screen max-w-6xl flex-col px-5 pb-14 pt-10 lg:px-8 lg:pt-14"}
    [:div {:class "grid flex-1 grid-cols-1 gap-8 lg:grid-cols-[18rem,1fr,16rem] lg:items-start"}
     [LeftPane {:toc []}]
     [:main {:class "min-w-0 space-y-10 rounded-2xl border border-slate-800/60 bg-slate-900/60 p-6 shadow-glow backdrop-blur lg:order-none"}
      props.children]
     [Aside {:ident [:blog/id (id)]}]]]])

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
