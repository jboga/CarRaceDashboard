class RTDataView extends Backbone.View

  el: '#rtdata'

  initialize: ()->
    @speedGauge = new jGauge()
    @speedGauge.id = 'speedGauge'
    @speedGauge.autoPrefix = autoPrefix.si
    @speedGauge.imagePath = 'assets/img/jgauge_face_taco.png'
    @speedGauge.segmentStart = -225
    @speedGauge.segmentEnd = 45
    @speedGauge.width = 170
    @speedGauge.height = 170
    @speedGauge.needle.imagePath = 'assets/img/jgauge_needle_taco.png'
    @speedGauge.needle.xOffset = 0
    @speedGauge.needle.yOffset = 0
    @speedGauge.label.yOffset = 80
    @speedGauge.label.color = '#fff'
    @speedGauge.label.precision = 0
    @speedGauge.label.suffix = 'km/h'
    @speedGauge.ticks.labelRadius = 45
    @speedGauge.ticks.labelColor = '#0ce'
    @speedGauge.ticks.start = 0
    @speedGauge.ticks.end = 300
    @speedGauge.ticks.count = 7
    @speedGauge.ticks.color = 'rgba(0, 0, 0, 0)'
    @speedGauge.range.color = 'rgba(0, 0, 0, 0)'
    @speedGauge.init()
    speed = @model.get('speed')
    @speedGauge.setValue(speed)
    dist = @model.get('distance')
    @ctx = $('#distance')[0].getContext('2d')
    @odo = new odometer(@ctx, {
      height: 40
      digits: 5
      decimals: 1
      value: dist
      wobbleFactor: 0.07
    })

    @model.on('change', @updateRTData)

  updateRTData: ()=>
    @speedGauge.setValue(@model.get('speed'))
    @odo.setValue(@model.get('dist'))

  updateRTEvent: (event)=>
    @model.set(event.type, event.value)


window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.RTDataView = RTDataView

