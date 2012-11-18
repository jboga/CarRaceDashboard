class DriverSelectorView extends Backbone.View

  el: '#carSel'

  initialize: ()->
    @model.on('add', @addAll)

  events:
    "click a": 'changeSelection'

  addAll: ()=>
    $(@el).html('')
    @addOne car for car in @model.models

  addOne: (car)=>
    $(@el).append('<li><a href="#" name="'+ car.get('name') + '">'+ car.get('name') + '</a></li>')

  changeSelection: (event)->
    carName = event.srcElement.name
    car = _.find(@model.models, (aCar)-> aCar.get('name') is carName)
    app.routers.RaceDashboardRouter.rtDataView.changeModel(car)


window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.DriverSelectorView = DriverSelectorView