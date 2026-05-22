package com.example.proyectofinalmobiles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * Developed by redleader
 */
public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;
    private OnProductoClickListener listener;

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
        void onEditarClick(Producto producto);
        void onDisponibilidadClick(Producto producto, boolean nuevoEstado);
    }

    public ProductoAdapter(List<Producto> listaProductos, OnProductoClickListener listener) {
        this.listaProductos = listaProductos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto prod = listaProductos.get(position);

        holder.tvNombre.setText(prod.getNombre());
        holder.tvDescripcion.setText(prod.getDescripcion());
        holder.tvPrecio.setText(String.format("$%.2f", prod.getPrecio()));

        if (prod.getImagenUrl() != null && !prod.getImagenUrl().isEmpty()) {
            String urlCompleta = "http://10.0.2.2:8080/uploads/" + prod.getImagenUrl();
            Picasso.get().load(urlCompleta).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivFoto);
        } else {
            holder.ivFoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        if (prod.isDisponible()) {
            holder.itemView.setAlpha(1.0f);
            holder.btnEliminar.setText("No Disponible");
            holder.btnEliminar.setIconResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            holder.itemView.setAlpha(0.5f);
            holder.btnEliminar.setText("Disponible");
            holder.btnEliminar.setIconResource(android.R.drawable.ic_input_add);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductoClick(prod);
        });

        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditarClick(prod);
        });

        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDisponibilidadClick(prod, !prod.isDisponible());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos != null ? listaProductos.size() : 0;
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDescripcion, tvPrecio;
        ImageView ivFoto;
        MaterialButton btnEditar, btnEliminar;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvItemNombreProducto);
            tvDescripcion = itemView.findViewById(R.id.tvItemtextPrecioProducto);
            tvPrecio = itemView.findViewById(R.id.tvItemPrecioProducto);
            ivFoto = itemView.findViewById(R.id.ivItemProducto);
            btnEditar = itemView.findViewById(R.id.btnItemEditarProducto);
            btnEliminar = itemView.findViewById(R.id.btnItemEliminarProducto);
        }
    }
}