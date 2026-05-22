package com.example.proyectofinalmobiles;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class MetricasVendedorActivity extends AppCompatActivity {
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metricas_vendedor);

        try {

            barChart = findViewById(R.id.barChart);

            mostrarGrafica();

        } catch (Exception e) {

            Toast.makeText(this,
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarGrafica() {

        ArrayList<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(1, 50));
        entries.add(new BarEntry(2, 80));
        entries.add(new BarEntry(3, 120));
        entries.add(new BarEntry(4, 150));

        BarDataSet dataSet =
                new BarDataSet(entries, "Ventas");

        BarData data = new BarData(dataSet);

        barChart.setData(data);

        barChart.invalidate();
    }
}
