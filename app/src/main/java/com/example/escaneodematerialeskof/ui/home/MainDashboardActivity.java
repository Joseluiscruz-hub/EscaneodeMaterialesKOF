package com.example.escaneodematerialeskof.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.escaneodematerialeskof.*;
import com.example.escaneodematerialeskof.dashboard.DashboardComparacionActivity;
import com.example.escaneodematerialeskof.ui.comparacion.ComparacionTiempoRealActivity;
import com.example.escaneodematerialeskof.ui.gestion.GestionReinicioActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class MainDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    // Cards para las funciones principales (ahora CardView)
    private CardView cardCapturarInventario;
    private CardView cardDashboard;
    private CardView cardResumen;
    private CardView cardComparar;
    private CardView cardImportar;

    // Botones de acciones rápidas (pueden no existir en el nuevo layout)
    private Button btnAjusteInventario;
    private Button btnConfiguracion;
    private Button btnReiniciarComparacion;
    private Button btnGestionReinicio;

    private FloatingActionButton fabScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dashboard_premium);

        initializeViews();
        setupToolbar();
        setupNavigationDrawer();
        setupClickListeners();

        // Manejo moderno del botón Atrás
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void initializeViews() {
        // Inicializar vistas del layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        // Cards principales (usando los IDs correctos del nuevo layout)
        cardCapturarInventario = findViewById(R.id.card_capturar_inventario);
        cardDashboard = findViewById(R.id.card_dashboard);
        cardResumen = findViewById(R.id.card_resumen);
        cardComparar = findViewById(R.id.card_comparar);
        cardImportar = findViewById(R.id.card_importar);

        // Botones de acciones rápidas (pueden no existir)
        btnAjusteInventario = findViewById(R.id.btn_ajuste_inventario);
        btnConfiguracion = findViewById(R.id.btn_configuracion);
        btnReiniciarComparacion = findViewById(R.id.btn_reiniciar_comparacion);
        btnGestionReinicio = findViewById(R.id.btn_gestion_reinicio);

        fabScan = findViewById(R.id.fab_scan);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("KOF Materiales");
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                android.R.string.ok,
                android.R.string.cancel
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupClickListeners() {
        // Click listeners para las cards principales (con validación null)
        if (cardCapturarInventario != null) {
            cardCapturarInventario.setOnClickListener(v -> mostrarDialogoTiposEscaneo());
        }

        if (cardDashboard != null) {
            cardDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(this, DashboardComparacionActivity.class);
                startActivity(intent);
            });
        }

        if (cardResumen != null) {
            cardResumen.setOnClickListener(v -> {
                Intent intent = new Intent(this, NewInventarioResumenActivity.class);
                startActivity(intent);
            });
        }

        if (cardComparar != null) {
            cardComparar.setOnClickListener(v -> {
                Intent intent = new Intent(this, ComparacionTiempoRealActivity.class);
                startActivity(intent);
            });
        }

        if (cardImportar != null) {
            cardImportar.setOnClickListener(v -> {
                Intent intent = new Intent(this, InventoryComparisonActivity.class);
                startActivity(intent);
            });
        }

        // Click listeners para botones de acciones rápidas (con validación null)
        if (btnAjusteInventario != null) {
            btnAjusteInventario.setOnClickListener(v -> {
                Intent intent = new Intent(this, AjusteInventarioActivity.class);
                startActivity(intent);
            });
        }

        if (btnConfiguracion != null) {
            btnConfiguracion.setOnClickListener(v -> {
                Intent intent = new Intent(this, CapturaInventarioActivity.class);
                startActivity(intent);
            });
        }

        if (btnReiniciarComparacion != null) {
            btnReiniciarComparacion.setOnClickListener(v -> reiniciarComparacion());
        }

        if (btnGestionReinicio != null) {
            btnGestionReinicio.setOnClickListener(v -> {
                Intent intent = new Intent(this, GestionReinicioActivity.class);
                startActivity(intent);
            });
        }

        // FAB para escaneo rápido (con validación null)
        if (fabScan != null) {
            fabScan.setOnClickListener(v -> {
                Intent intent = new Intent(this, CapturaInventarioActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_inicio) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_capturar_inventario) {
            Intent intent = new Intent(this, CapturaInventarioActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_dashboard) {
            Intent intent = new Intent(this, DashboardComparacionActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_resumen_inventario) {
            Intent intent = new Intent(this, NewInventarioResumenActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_comparar_inventarios) {
            Intent intent = new Intent(this, InventoryComparisonActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_comparacion_tiempo_real) {
            Intent intent = new Intent(this, ComparacionTiempoRealActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gestion_almacenes) {
            Intent intent = new Intent(this, com.example.escaneodematerialeskof.ui.almacenes.GestionAlmacenesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ajuste_inventario) {
            Intent intent = new Intent(this, AjusteInventarioActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gestion_reinicio) {
            // Abrir la pantalla de gestión de reinicio corregida
            Intent intent = new Intent(this, GestionReinicioActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_configuracion) {
            Intent intent = new Intent(this, CapturaInventarioActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_reiniciar_comparacion) {
            reiniciarComparacion();
        } else if (id == R.id.nav_reiniciar_completo) {
            reiniciarCompleto();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void reiniciarComparacion() {
        // Abrir la pantalla de gestión de reinicio
        Intent intent = new Intent(this, GestionReinicioActivity.class);
        startActivity(intent);
    }

    private void reiniciarCompleto() {
        // Abrir la pantalla de gestión de reinicio
        Intent intent = new Intent(this, GestionReinicioActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoTiposEscaneo() {
        // Inflar el layout personalizado
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_seleccionar_tipo_escaneo, null);

        // Crear el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Configurar las opciones del diálogo
        LinearLayout optionRumba = dialogView.findViewById(R.id.option_rumba);
        LinearLayout optionPallets = dialogView.findViewById(R.id.option_pallets);
        LinearLayout optionManual = dialogView.findViewById(R.id.option_manual);
        Button btnCancelar = dialogView.findViewById(R.id.btn_cancelar);

        // Click listeners para cada opción
        optionRumba.setOnClickListener(v -> {
            dialog.dismiss();
            iniciarEscaneoRumba();
        });

        optionPallets.setOnClickListener(v -> {
            dialog.dismiss();
            iniciarEscaneoPallets();
        });

        optionManual.setOnClickListener(v -> {
            dialog.dismiss();
            iniciarCapturaManual();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        // Mostrar el diálogo
        dialog.show();
    }

    private void iniciarEscaneoRumba() {
        // Lógica para iniciar el escaneo de rumba
        Toast.makeText(this, getString(R.string.toast_escanear_rumba), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, CapturaInventarioActivity.class);
        intent.putExtra("tipo_escaneo", "rumba");
        startActivity(intent);
    }

    private void iniciarEscaneoPallets() {
        // Lógica para iniciar el escaneo de pallets
        Toast.makeText(this, getString(R.string.toast_escanear_pallet), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, CapturaInventarioActivity.class);
        intent.putExtra("tipo_escaneo", "pallet");
        startActivity(intent);
    }

    private void iniciarCapturaManual() {
        // Lógica para iniciar la captura manual
        Toast.makeText(this, getString(R.string.toast_manual), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, CapturaInventarioActivity.class);
        intent.putExtra("tipo_escaneo", "manual");
        startActivity(intent);
    }
}
