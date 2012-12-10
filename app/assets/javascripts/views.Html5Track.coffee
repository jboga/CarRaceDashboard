class Html5TrackView extends Backbone.View

  el: '#track'
  widthM: 400
  heightM: 300


  initialize: ()->
    @stage = new Kinetic.Stage({
    container: 'track'
    width: @widthM
    height: @heightM
    })

    $.ajax(
      type: "GET",
      url: "/track",
      contentType: "application/json",
      data: ""
    ).done (result)=>
      @drawTrack(result)

    @model.on('change:pos', @updatePos)

  drawTrack: (result)=>
    @layer = new Kinetic.Layer()
    @minX = _.min(_.pluck(result, 'x'))
    @minY = _.min(_.pluck(result, 'y'))
    @maxX = _.max(_.pluck(result, 'x'))
    @maxY = _.max(_.pluck(result, 'y'))
    @lonW = @maxX - @minX
    @latH = @maxY - @minY
    arr = _.map(result, (coord)=> [ @calcX(coord.x) , @calcY(coord.y)])
    track = _.flatten(arr)
    @poly = new Kinetic.Polygon({
      points: track,
      stroke: 'white',
      strokeWidth: 3
    })
    @layer.add(@poly)
    @stage.add(@layer)


  calcX: (lon)=>
    @widthM * 0.1 + (lon - @minX) * @widthM * 0.8 / @lonW

  calcY: (lat)=>
    @heightM - (@heightM * 0.1 + (lat - @minY) * @heightM * 0.8 / @latH)

  updatePos: (car)=>
    theMarker = car.get('marker')
    pos = car.get('pos')
    if theMarker
      theMarker.x = @calcX(pos.longitude)
      theMarker.y = @calcY(pos.latitude)
    else
      imageObj = new Image()
      imageObj.src = car.get('iconUrl')
      marker = new Kinetic.Image({
        x: @calcX(pos.longitude)
        y: @calcY(pos.latitude)
        image: imageObj
      })
      car.set('marker', marker)

window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.Html5TrackView = Html5TrackView