class Html5TrackView extends Backbone.View

  el: '#track'

  initialize: ()->
    @stage = new Kinetic.Stage({
      container: 'track'
      width: 400
      height: 300
    })

    $.ajax(
      type: "GET",
      url: "/track",
      contentType: "application/json",
      data: ""
    ).done (result)=>
      @layer = new Kinetic.Layer()

      @poly = new Kinetic.Polygon({
        points: result,
        stroke: 'white',
        strokeWidth: 3
      })
      @layer.add(@poly)
      @stage.add(@layer)


window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.Html5TrackView = Html5TrackView