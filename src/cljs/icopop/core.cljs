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
     "Mouse over the render area to change the scale of the icosahedron and the triangles composing it. The y-axis changes the scale of the icosahedron, while the x-axis changes the scale of the individual triangle faces. There are 20 triangle faces constructed as individual Mesh objects so that they can be manipulated independently. They are constructed from a list of known verticies on an icosahedron, but each triangle is constructed with points normalized around origin. Then they are added to a parent object, and translated to the midpoint of the original face position. Thus when the triangle meshes are scaled, they are scaled relative to their true position in the icosahedron shape."]
    [:ol
     [:li [:a {:href "//github.com/timothypratley"}
           "Code on github"]]
     [:li [:a {:href "timothypratley.blogspot.com"}
           "My blog"]]
     [:li [:a {:href "http://stackoverflow.com/questions/27383979/how-do-i-make-the-triangular-side-areas-increase-and-decrease-on-an-icosahedron"}
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
