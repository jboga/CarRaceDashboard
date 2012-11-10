class GraphView extends Backbone.View

  initialize: ()->
    @graph = new Rickshaw.Graph(
      element: document.getElementById(@id)
      renderer: "bar"
      width: 1000
      series: [
        data: [
          x: 1325376000
          y: 0
        ,
          x: 1328054400
          y: 0
        ,
          x: 1330560000
          y: 0
        ,
          x: 1333238400
          y: 0
        ,
          x: 1335830400
          y: 0
        ,
          x: 1338508800
          y: 0
        ,
          x: 1341100800
          y: 0
        ,
          x: 1343779200
          y: 0
        ,
          x: 1346457600
          y: 0
        ]
        color: '#e36b23'
        name: "Nb jours"
      ]
    )
    @graph.renderer.unstack = true
    @graph.render()
    axes = new Rickshaw.Graph.Axis.Time(graph: @graph)
    axes.render()
    hoverDetail = new Rickshaw.Graph.HoverDetail(graph: @graph)
    slider = new Rickshaw.Graph.RangeSlider(
      graph: @graph,
      element: $('#'+ @id + '-slider')
    )


  setDomain: (dom)->
    @domain = dom
    @update('all', 'all')
    @

  render: ()->
    @graph.render()

  update: (trigs, codes)->
    $.ajax(
      type: "GET",
      url: "/histo?domain=" + @domain + "&interval=month&trigs=" + trigs + "&codes=" + codes,
      contentType: "application/json",
      data: ""
    ).done (result)=>
      @graph.series[0].data = result
      @graph.render()

window.app=window.app || {}
window.app.views=window.app.views || {}
window.app.views.GraphView=GraphView



