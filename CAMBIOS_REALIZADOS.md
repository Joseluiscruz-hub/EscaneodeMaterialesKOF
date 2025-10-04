# Cambios planificados (fase actual)

Este archivo se actualizará al completar la implementación del issue: "Alinear y asegurar funcionalidad de todos los botones".

Fase 1 (Plan y auditoría):
- [x] Crear plan de trabajo y comunicarlo.
- [ ] Auditar todos los layouts y menús para inventariar botones y mapear acciones.
- [ ] Revisar wiring (setOnClickListener/onClick) en Activities/Fragments.
- [ ] Verificar Navigation Drawer/Toolbar/Overflow.
- [ ] Ajustar alineación/posicionamiento de botones clave.
- [ ] Validación y pruebas manuales.

Se irá completando con la lista de botones/pantallas corregidos y cualquier nota de compatibilidad.

## Fase 2 (Correcciones realizadas en esta sesión)
- [x] Normalización de valores de tipo_escaneo a minúsculas en emisores:
  - HomeNewActivity: "rumba", "pallet", "manual".
  - HomeFixedActivity: "rumba", "pallet", "manual".
  - ui.home.MainDashboardActivity (Java): corregido "pallets" -> "pallet".
- [x] Robustez del receptor:
  - CapturaInventarioActivity: se aplica lowercase() al leer el extra en onCreate, en el scanLauncher y en validarCamposObligatorios().

Impacto: Al seleccionar el tipo de escaneo desde Home/Dashboard, ahora se aplica el modo correcto en Captura de Inventario. Esto corrige botones/acciones que parecían no funcionar por desajuste de valores.
