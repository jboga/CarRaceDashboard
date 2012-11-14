class StatDataView extends Backbone.View

  el: '#statdata'
  rowTemplate: _.template( $('#template-rowstats').html() )

  initialize: ()->
    @model.on('all', @updateStats)
    @body = $(@el).find('tbody')

  updateStats: ()=>
    @body.html('')
    @addRow car for car in @model.models

  addRow: (car)=>
    @body.append(@rowTemplate(car.toJSON()))


window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.StatDataView = StatDataView

