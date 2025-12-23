(ns md
  (:require ["solid-js" :refer [createResource createMemo]]
            ["@shikijs/markdown-it/async" :refer [fromAsyncCodeToHtml]]
            ["markdown-it-async" :as MarkdownItAsync]
            ["shiki" :refer [codeToHtml]]))

(def md ((:default MarkdownItAsync)))

(defn shiki-md-init []
  (.use md
        (fromAsyncCodeToHtml
         codeToHtml
         {:themes {:light "vitesse-light"
                   :dark  "vitesse-dark"}}))
  md)

(defn render-md-async
  "Returns promise"
  [s]
  (.renderAsync md s))

(defn createMarkdownResource [md-source]
  (createResource md-source render-md-async))

(defn Markdown [{:keys [source]}]
  (let [[html] (createMarkdownResource source)]
    html))
