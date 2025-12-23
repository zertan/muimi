(ns subscribe
  (:require ["solid-js" :refer [createMemo]]))

(defn- env [key]
  (some-> js/import.meta .-env (aget key)))

(defn- mailchimp-config []
  (let [form-url (env "VITE_MAILCHIMP_FORM_URL")
        data-center (env "VITE_MAILCHIMP_DC")
        audience (env "VITE_MAILCHIMP_U")
        list-id (env "VITE_MAILCHIMP_ID")]
    {:action (or form-url
                 (when data-center
                   (str "https://" data-center ".list-manage.com/subscribe/post"))
                 "https://YOUR_DC.list-manage.com/subscribe/post")
     :u (or audience "")
     :id (or list-id "")}))

(defn SubscribeCard []
  (let [cfg (createMemo mailchimp-config)]
    #jsx
    [:div {:class "rounded-2xl border border-slate-800/60 bg-slate-900/60 p-5 shadow-glow backdrop-blur"}
     [:div {:class "mb-3 text-xs uppercase tracking-[0.2em] text-slate-400"} "Newsletter"]
     [:h2 {:class "text-lg font-semibold text-white"} "Stay in the loop"]
     [:p {:class "mt-1 text-sm text-slate-400 leading-relaxed"}
      "Weekly notes on how muimi experiments are built and shipped."]
     [:form {:class "mt-4 space-y-3"
             :action (:action (cfg))
             :method "post"
             :target "_blank"
             :noValidate true}
      [:input {:type "hidden" :name "u" :value (:u (cfg))}]
      [:input {:type "hidden" :name "id" :value (:id (cfg))}]
      [:label {:class "block text-sm font-medium text-slate-300"}
       [:span {:class "sr-only"} "Email"]
       [:input {:name "EMAIL"
                :type "email"
                :required true
                :placeholder "you@example.com"
                :class "w-full rounded-lg border border-slate-700/70 bg-slate-900 px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 shadow-inner shadow-black/20 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/40"}]]
      [:button {:type "submit"
                :class "w-full rounded-lg bg-gradient-to-r from-slate-800 via-slate-700 to-slate-800 px-3 py-2 text-sm font-semibold text-slate-50 ring-1 ring-slate-700/80 transition hover:-translate-y-[1px] hover:from-slate-700 hover:to-slate-900 hover:ring-slate-600 active:translate-y-0"}
       "Subscribe"]
      [:p {:class "text-xs text-slate-500"}
       "We'll route you through Mailchimp. Add your data center, audience (u), and list (id) IDs to "
       [:code {:class "rounded bg-slate-800 px-1.5 py-0.5 text-[11px] text-slate-200"} "VITE_MAILCHIMP_*"]
       " env vars."]]]))
