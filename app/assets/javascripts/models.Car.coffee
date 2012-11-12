class Car extends Backbone.Model

  initialize: ()->
    setTimeout(
      ()=>
        @set('speed',123)
        @set('distance', @get('distance') + 1)
      , 500)


window.app=window.app || {}
window.app.models=window.app.models || {}
window.app.models.Car=Car