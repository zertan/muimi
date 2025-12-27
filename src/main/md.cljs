(ns md
  (:require ["solid-js" :refer [createResource createMemo]]
            ["@shikijs/markdown-it/async" :refer [fromAsyncCodeToHtml]]
            ["markdown-it-async" :as MarkdownItAsync]
            ["shiki" :refer [createHighlighter]]

            ;; themes
            ["@shikijs/themes/vitesse-light" :as vitesseLight*]
            ["@shikijs/themes/vitesse-dark" :as vitesseDark*]

            ;; langs
            ["@shikijs/langs/javascript" :as javascript*]
            ["@shikijs/langs/typescript" :as typescript*]
            ["@shikijs/langs/json" :as json*]
            ["@shikijs/langs/bash" :as bash*]
            ["@shikijs/langs/yaml" :as yaml*]
            ["@shikijs/langs/html" :as html*]
            ["@shikijs/langs/css" :as css*]
            ["@shikijs/langs/markdown" :as markdown*]
            ["@shikijs/langs/clojure" :as clojure*]
            ["@shikijs/langs/python" :as python*]
            ["@shikijs/langs/wolfram" :as wolfram*]))


;; Normalize all module imports to the actual theme/lang objects
(def vitesseLight (:default vitesseLight*))
(def vitesseDark  (:default vitesseDark*))

(def javascript (:default javascript*))
(def typescript (:default typescript*))
(def json       (:default json*))
(def bash       (:default bash*))
(def yaml       (:default yaml*))
(def html       (:default html*))
(def css        (:default css*))
(def markdown   (:default markdown*))
(def clojure    (:default clojure*))
;; (def clojurescript (:default clojurescript*))
(def python     (:default python*))
(def wolfram    (:default wolfram*))

(def md ((:default MarkdownItAsync)))

(def ^:private theme-light "vitesse-light")
(def ^:private theme-dark  "vitesse-dark")

(defonce ^:private highlighter* (atom nil))

(def ^:private langs
  ;; language id -> language module
  #js {"javascript" javascript
       "typescript" typescript
;       "json" json
       "bash" bash
       "yaml" yaml
       "html" html
       "css" css
       "markdown" markdown
       "clojure" clojure
;       "clojurescript" clojurescript
       "python" python
       "wolfram" wolfram})

(defn ^:private normalize-lang [lang]
  (case lang
    "js" "javascript"
    "jsx" "javascript"
    "ts" "typescript"
    "tsx" "typescript"
    "sh" "bash"
    "shell" "bash"
    "yml" "yaml"
    "wl" "wolfram"
    "mma" "wolfram"
    ;; default:
    (or lang "text")))

(defn ^:private supported-lang? [lang]
  (some? (aget langs lang)))

(defn ^:private ensure-highlighter! []
  (if @highlighter*
    (js/Promise.resolve @highlighter*)
    (-> (createHighlighter
         {:themes #js [vitesseLight vitesseDark]
          :langs  (js/Object.values langs)})
        (.then (fn [h]
                 (reset! highlighter* h)
                 h)))))

(defn ^:private codeToHtmlLimited [code lang options]
  (let [options (or options #js {})
        lang*   lang
        theme   (or (aget options "theme") theme-dark)
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

;; ---------- Markdown fetch + render ----------

(defn render-md-async [s]
  
  (.renderAsync md s))

(defn with-base [path]
  ;; Vite-safe base (works on gh-pages base too)
  (let [base (.-BASE_URL (.-env js/import.meta))]
    ;; base usually ends with "/" and path begins with "/"
    (if (and base path)
      (str (subs base 0 (dec (count base))) path)
      path)))

(defn createMarkdownResource [source$]
  ;; source$ can be:
  ;; - a string url/path
  ;; - an accessor fn that returns a string url/path
  (createResource
    (fn []
      (let [s (if (fn? source$) (source$) source$)]
        (when s (with-base s))))
    (fn [url]
      (-> (js/fetch url)
          (.then (fn [r] (.text r)))
          (.then render-md-async)))))

(defn Markdown [{:keys [source]}]
  (let [[html] (createMarkdownResource source)]
    #jsx [:div {:class "markdown prose prose-invert max-w-none"
                :innerHTML (or (html) "<p>Loading…</p>")}]))
