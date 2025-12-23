(ns md
  (:require ["solid-js" :refer [createResource createMemo]]
            ["@shikijs/markdown-it/async" :refer [fromAsyncCodeToHtml]]
            ["markdown-it-async" :as MarkdownItAsync]
            ["shiki" :refer [createHighlighter bundledLanguages bundledThemes]]))

(def md ((:default MarkdownItAsync)))

;; ---- Shiki: only load what you need ----

(defonce ^:private highlighter* (atom nil))

(def ^:private theme-light "vitesse-light")
(def ^:private theme-dark  "vitesse-dark")

(def ^:private langs
  ;; Keep this list tight to reduce bundle size.
  ;; Keys are the language ids you expect in markdown fences.
  ;; Add/remove as needed.
  #js {:javascript      (aget bundledLanguages "javascript")
       :typescript      (aget bundledLanguages "typescript")
       :json            (aget bundledLanguages "json")
       :bash            (aget bundledLanguages "bash")
       :yaml            (aget bundledLanguages "yaml")
       :html            (aget bundledLanguages "html")
       :css             (aget bundledLanguages "css")
       :markdown        (aget bundledLanguages "markdown")
       :clojure         (aget bundledLanguages "clojure")
       :clojurescript   (aget bundledLanguages "clojurescript")
       :python          (aget bundledLanguages "python")
       ;; Shiki language id is typically "wolfram" (Wolfram Language / Mathematica)
       :wolfram         (aget bundledLanguages "wolfram")})

(defn ^:private ensure-highlighter! []
  (if @highlighter*
    (js/Promise.resolve @highlighter*)
    (-> (createHighlighter
         #js {:themes #js [(aget bundledThemes theme-light)
                           (aget bundledThemes theme-dark)]
              :langs  (js/Object.values langs)})
        (.then (fn [h]
                 (reset! highlighter* h)
                 h)))))

(defn ^:private normalize-lang [lang]
  ;; markdown-it gives strings like "ts" "js" etc sometimes.
  ;; Map common aliases to our supported set.
  (case lang
    "js" "javascript"
    "ts" "typescript"
    "yml" "yaml"
    "wl" "wolfram"
    "mma" "wolfram"
    ;; default:
    lang))

(defn ^:private supported-lang? [lang]
  (some? (aget langs lang)))

(defn ^:private codeToHtmlLimited
  [code lang options]
  (let [options (or options #js {})
        lang*   (normalize-lang (or lang "text"))
        theme   (or (aget options "theme") theme-light)
        lang2   (if (supported-lang? lang*) lang* "text")]
    (-> (ensure-highlighter!)
        (.then (fn [h]
                 (.codeToHtml h code #js {:lang lang2 :theme theme}))))))

(defn shiki-md-init []
  (.use md
        (fromAsyncCodeToHtml
         codeToHtmlLimited
         #js {:themes #js {:light theme-light
                           :dark  theme-dark}}))
  md)

#_(defn render-md-async
  "Returns promise"
  [s]
  (.renderAsync md s))

#_(defn createMarkdownResource [md-source]
  (createResource md-source render-md-async))

#_(defn Markdown [{:keys [md]}]
  (let [[html] (createMarkdownResource md)]
    html))

(defn render-md-async [s]
  (.renderAsync md s))

(defn fetch-markdown [url]
  (js/console.log "fetch")
  (-> (js/fetch (str "http://localhost:5173" url))
      (.then (fn [r] (.text r)))))

#_(defn createMarkdownResource [source-url]
  ;; resource key is the URL string -> refetches when URL changes
  (createResource source-url
                  (fn [url] (-> (fetch-markdown url)
                                (.then render-md-async)))))

(defn createMarkdownResource [url$]
  (createResource
    url$
    (fn [url]
      (-> (js/fetch url)
          (.then (fn [r] (.text r)))
          (.then render-md-async)))))

(defn Markdown [{:keys [source]}]
  (let [[html] (createMarkdownResource source)]
    #jsx [:div {:class "markdown"
                :innerHTML (if (html) (html) "<p>Loading…</p>")}]))
