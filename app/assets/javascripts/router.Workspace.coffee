class Workspace extends Backbone.Router

  routes:
    '*filter': 'setFilter'

  setFilter: (param)->
    window.app.TodoFilter = param.trim() || ''
    app.collections.Todos.trigger('reset')

$ ->
  window.app.router.TodoRouter = new Workspace()
  Backbone.history.start()


window.app=window.app || {}
window.app.router=window.app.router || {}
