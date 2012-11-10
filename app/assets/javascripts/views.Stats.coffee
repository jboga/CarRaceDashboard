class StatsView extends Backbone.View

  el: '#stats'
  template: _.template($('#template-stats').html())


  initialize: ()->
    @div = $(@el).find('div[name=stats]')
    @update('all', 'all')


  render: ()=>
    @$el.html('')
    $(@el).append(_.template($('#template-stats').html())(@model.toJSON()))
    @

  update: (trigs, codes)->
    $.ajax(
      type: "GET",
      url: "/stats?trigs=" + trigs + "&codes=" + codes,
      contentType: "application/json",
      data: ""
    ).done (result)=>
      @model = new app.models.Stats(result)
      @render()

window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.StatsView = StatsView