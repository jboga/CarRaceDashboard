class ClientsList extends Backbone.Collection
  model: app.models.FacetItem
  url: "/infos?domain=affect&info=client&trigs=all&codes=all"

class AffairesList extends Backbone.Collection
  model: app.models.FacetItem
  url: "/infos?domain=affect&info=code&trigs=all&codes=all"

class NonaffList extends Backbone.Collection
  model: app.models.FacetItem
  url: "/infos?domain=na&info=code&trigs=all&codes=all"

class CollList extends Backbone.Collection
  model: app.models.FacetItem
  url: "/infos?domain=affect&info=trig&trigs=all&codes=all"

class NACollList extends Backbone.Collection
  model: app.models.FacetItem
  url: "/infos?domain=na&info=trig&trigs=all&codes=all"

window.app=window.app || {}
window.app.collections=window.app.collections || {}
window.app.collections.Affaires=new AffairesList()
window.app.collections.Clients=new ClientsList()
window.app.collections.Colls=new CollList()
window.app.collections.NAColls=new NACollList()
window.app.collections.Nonaff=new NonaffList()

