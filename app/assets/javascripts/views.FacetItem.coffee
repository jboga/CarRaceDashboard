class FacetItemView extends Backbone.View

  tagName:  'li'
  className: 'checkbox'
  templateItem: _.template( $('#template-facet-item').html() )

  events:
    "click input[name='item']": 'updateModel'

  initialize: ()->
    @model.on('change:isSelected', @updateSelection)

  checkbox: ()-> @$el.find('input[name=item]')

  render: ()=>
    @$el.html( @templateItem( @model.toJSON() ) )
    @

  updateSelection: ()=>
    @checkbox().attr('checked', @model.get('isSelected'))

  updateModel: ()=>
    @model.set 'isSelected', @checkbox().attr('checked')

window.app=window.app || {}
window.app.views=window.app.views || {}
window.app.views.FacetItemView=FacetItemView