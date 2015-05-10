(ns icopop.core
  (:require [icopop.renderbox :as r]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react])
  (:import goog.History))

;; -------------------------
;; Views

(defn home-page []
  [:div
   [:h2 "Welcome to Icopop"]
   [r/renderbox]
   [:div {:style {:padding 100}}
    [:p {:style {:text-align "left"}}
     "Move your mouse cursor over the render area to change the scale of the icosahedron its component triangles.
The y-axis scales the icosahedron, while the x-axis scales the individual triangles.
There are 20 triangles constructed as individual mesh objects so that they can be scaled independently.
They are constructed from the 12 vertices of an icosahedron.
Each triangle is constructed with points centered around zero.
The triangle meshes are added to a parent object, and positioned at the midpoint of the face.
Thus when the triangles are scaled, they are scaled relative to their position in the icosahedron shape."]
    [:ol
     [:li [:a {:href "//github.com/timothypratley/icopop"}
           "Code on github"]]
     [:li [:a {:href "//timothypratley.blogspot.com/2015/05/icosahedron-disjoint-visual-effect.html"}
           "My blog post about it"]]
     [:li [:a {:href "//stackoverflow.com/questions/27383979/how-do-i-make-the-triangular-side-areas-increase-and-decrease-on-an-icosahedron"}
           "In response to stackoverflow question"]]]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
