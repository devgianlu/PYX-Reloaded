var drawerEl = document.querySelector('.mdc-persistent-drawer');
var MDCPersistentDrawer = mdc.drawer.MDCPersistentDrawer;
var drawer = new MDCPersistentDrawer(drawerEl);
document.querySelector('.demo-menu').addEventListener('click', function() {
  drawer.open = !drawer.open;
});
drawerEl.addEventListener('MDCPersistentDrawer:open', function() {
  console.log('Received MDCPersistentDrawer:open');
});
drawerEl.addEventListener('MDCPersistentDrawer:close', function() {
  console.log('Received MDCPersistentDrawer:close');
});
