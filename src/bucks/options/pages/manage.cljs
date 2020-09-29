(ns bucks.options.pages.manage
  (:require [bucks.options.state :as options]
            [re-frame.core :as rf]
            [bucks.options.components.select-currency :as select-currency]))


(defn page []
  [:div.columns.is-variable.is-8
   [:div.column.is-narrow
    (let [{:keys [base-currency fixer-api-key] :as o} @(rf/subscribe [::options/options])]
      [:div {:style {:max-width "250px"}}
       [:div.field
        [:label.label "Base Currency"]
        [:div.control
         [select-currency/component base-currency (fn [v] (options/set-option :base-currency v))]]
        [:div.help.is-warning "Changing this after you have imported entries with different exchange rates will cause undesirable behaviour"]]
       [:div.field
        [:label.label "Fixer Api Access Key"]
        [:div.control
         [:input.input {:value fixer-api-key
                        :on-change #(options/set-option :fixer-api-key (.. % -target -value))}]]
        [:div.help "Use " [:a {:href "https://fixer.io/" :target "_blank"} "fixer.io"]
         " for syncing exchange rates. Free account is sufficient."]]
       [:div.field
        [:label.label "Actions"]
        [:div.control
         [:button.button.is-danger
          {:on-click (fn []
                       (when (js/confirm "This cannot be un done")
                         (rf/dispatch [:bucks.app/clean])))}
          "CLEAR DATA"]]
        ]])]
   [:div.column.is-size-7
    [:h2.heading "Account Types"]
    [:div.content
     [:ul
      [:li [:b "Investment: "] "For tracking your investment growth"]
      [:li [:b "Budget: "] "For tracking non investment money (income, spend, etc.)"]
      [:li [:b "Bucket: "] "An account for tracking budgeted money saved for later (vacation, yearly expenses etc)"]]
     [:p "Data from both account types will be used to calculate things like savings rate and time till retirement."]]
    [:h2.heading "Entry Types"]
    [:div.content.is-size-7
     [:ul
      [:li "Investment"
       [:ul
        [:li [:b "Todo: "] "Lorum"]]]
      [:li "Budget"
       [:ul
        [:li [:b "Income: "] "Money that you earn"]
        [:li [:b "Expense: "] "Money that you spend"]
        [:li [:b "Refund: "] "Money that you get that is not considered income"]
        [:li [:b "Transfer: "] "Transfers into and from the account (That are not considered income or expenditure)"]]]
      [:li "Bucket"
       [:ul
        [:li [:b "In: "] "Money that moved in for saving"]
        [:li [:b "Out: "] "Money that moved out for spending"]]]]]]])
