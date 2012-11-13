class TrackSelectorView extends Backbone.View

  el: '#carSel'

  initialize: ()->
    @model.on('add', @addAll)


  addAll: ()=>
    $(@el).html('')
    @addOne car for car in @model.models

  addOne: (car)=>
    $(@el).append('<li><a href="#">'+ car.get('name') + '</a></li>')


window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.TrackSelectorView = TrackSelectorView