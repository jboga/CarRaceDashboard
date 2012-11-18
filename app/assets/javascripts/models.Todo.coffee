class Todo extends Backbone.Model

  defaults:
    title: ''
    completed: false

  toggle: ()->
    @save(
      completed: !@get('completed')
    )


window.app=window.app || {}
window.app.models=window.app.models || {}
window.app.models.Todo=Todo