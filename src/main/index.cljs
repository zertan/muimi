(ns index
  (:require ["solid-js" :refer [createContext createEffect]]
            ["solid-js/web" :refer [render]]
            ["@solidjs/router" :refer [Router]]
            ["@w3t-ab/sqeave" :as sqeave]
            ["./main.cljs" :refer [Main sample-posts]])
  (:require-macros [sqeave :refer [defc]]))

(def AppContext (createContext))

(defc Root [this {:keys [children] :or {} :ctx (sqeave/init-ctx! AppContext)}]
  (createEffect
   (fn []
     (sqeave/add! ctx {:blog/id 1
                       :blog/posts sample-posts
                       :blog/post [:post/id (:post/id (first sample-posts))]
                       :blog/category (:post/category (first sample-posts))}
                    {:replace true})))
  #jsx [AppContext.Provider {:value ctx}
        [:div {:class "app"}
         children]])

(let [e (js/document.getElementById "root")]
  (set! (aget e :innerHTML) "")
  (render (fn []
            #jsx [Router {:root Root}
                  [Main {:ident [:blog/id 1]}]]) e))
