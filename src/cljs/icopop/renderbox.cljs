(ns icopop.renderbox
  (:require [reagent.core :as reagent]
            [cljsjs.three]))

(defonce state
  (reagent/atom {:ico-scale 1
                 :face-scale 1}))

(def ico-scale 1)
(def face-scale 0.7)

(defn geo [points]
  (let [material (js/THREE.MeshBasicMaterial. #js {:color 0x00a000})
        wire (js/THREE.MeshBasicMaterial. #js {:color 0x000000
                                               :wireframe true})
        geometry (js/THREE.Geometry.)
        o (js/THREE.Object3D.)]
    (doseq [[px py pz] points]
      (.push (.-vertices geometry) (js/THREE.Vector3. px py pz)))
    (.push (.-faces geometry) (js/THREE.Face3. 0 1 2))
    (.computeBoundingSphere geometry)
    (.add o (js/THREE.Mesh. geometry material))
    (.add o (js/THREE.Mesh. geometry wire))))

(def t (/ (+ 1 (js/Math.sqrt 5)) 2))

(def vertices
  [[-1 t 0] [1 t 0] [-1 (- t) 0] [1 (- t) 0]
   [0 -1 t] [0 1 t] [0 -1 (- t)] [0 1 (- t)]
   [t 0 -1] [t 0 1] [(- t) 0 -1] [(- t) 0 1]])

(def indices
  [[0 11  5]    [0  5  1]    [0  1  7]    [0  7 10]    [0 10 11]
   [1  5  9]    [5 11  4]   [11 10  2]   [10  7  6]    [7  1  8]
   [3  9  4]    [3  4  2]    [3  2  6]    [3  6  8]    [3  8  9]
   [4  9  5]    [2  4 11]    [6  2 10]    [8  6  7]    [9  8  1]])

(defn average [v]
  (/ (apply + v) (count v)))

(defn transpose [v]
  (apply map vector v))

(defn icos []
  (let [o (js/THREE.Object3D.)]
    (doseq [triangle indices
            :let [vs (map vertices triangle)
                  [x y z :as a] (map average (transpose vs))
                  nvs (for [v vs]
                        (map - v a))
                  face (geo nvs)]]
      (.add o face)
      (.translateX face x)
      (.translateY face y)
      (.translateZ face z))
    o))

(defn add-cube [scene]
  (let [geometry (js/THREE.CubeGeometry. 1 1 1)
        material (js/THREE.MeshBasicMaterial. #js {:color 0x00ff00
                                                   :wireframe true})
        cube (js/THREE.Mesh. geometry material)]
    (.add scene cube)
    cube))

(defn offset [element]
  (let [body (.getBoundingClientRect js/document.body)
        elem (.getBoundingClientRect element)]
    [(- (.-left elem) (.-left body))
     (- (.-top elem) (.-top body))]))

(defn renderbox* []
  [:canvas {:on-mouse-move (fn [e]
                             (let [[ox oy] (offset (.-target e))
                                   x (- (.-pageX e) ox)
                                   y (- (.-pageY e) oy)]
                               (set! face-scale (/ 100 x))
                               (set! ico-scale (/ 100 y))))
            :style {:width "400"
                    :height "400"}}])

(defn renderbox []
  (let [running (atom false)]
    (reagent/create-class
     {:display-name "renderbox"
      :reagent-render renderbox*
      :component-did-mount
      (fn did-mount [this]
        (println "did-mount")
        (let [node (.getDOMNode this)
              width (.-offsetWidth node)
              height (.-offsetHeight node)
              scene (js/THREE.Scene.)
              thing (icos)
              camera (js/THREE.PerspectiveCamera. 75 (/ width height) 0.1 1000)
              renderer (js/THREE.WebGLRenderer. #js {:canvas node
                                                     :antialias true
                                                     :alpha true})]
          (.add scene thing)
          (.setSize renderer width height)
          (set! (.. camera -position -z) 5)
          (.render renderer scene camera)
          (reset! running true)
          ((fn render []
             (when @running
               (js/requestAnimationFrame render)
               (set! (.. thing -rotation -y) (+ 0.01 (.. thing -rotation -y)))
               (set! (.. thing -rotation -x) (+ 0.003 (.. thing -rotation -x)))
               (set! (.. thing -rotation -z) (+ 0.001 (.. thing -rotation -z)))
               (.set (.-scale thing) ico-scale ico-scale ico-scale)
               (doseq [face (.-children thing)]
                 (.set (.-scale face) face-scale face-scale face-scale))
               (.render renderer scene camera))))))
      :component-will-unmount
      (fn will-unmount [this]
        (println "will-unmount")
        (reset! running false))})))
