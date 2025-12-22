(ns index
  (:require ["solid-js" :refer [createContext]]
            ["solid-js/web" :refer [render]]
            ["@w3t-ab/sqeave" :as sqeave]
            ["./main.cljs" :refer [Main]])
  (:require-macros [sqeave :refer [defc]]))

(def AppContext (createContext))

(defc Root [this {:keys [] :or {} :ctx (sqeave/init-ctx! AppContext)}]
  #jsx [AppContext.Provider {:value this.-ctx}
        [Main {:ident [:main/id 0]}]])

(let [e (js/document.getElementById "root")]
  (set! (aget e :innerHTML) "")
  (render Root e))
