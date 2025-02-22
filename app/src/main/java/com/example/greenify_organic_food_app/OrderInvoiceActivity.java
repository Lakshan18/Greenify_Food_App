package com.example.greenify_organic_food_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class OrderInvoiceActivity extends AppCompatActivity {

    private TextView orderIdTextView, orderDateTextView, orderStatusTextView, orderTotalTextView, productName;
    private Button downloadReceiptButton;
    private ImageView productImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_invoice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        productImageView = findViewById(R.id.order_image_reci);
        productName = findViewById(R.id.product_name_reci);
        orderIdTextView = findViewById(R.id.order_no_reci);
        orderDateTextView = findViewById(R.id.order_date_reci);
        orderStatusTextView = findViewById(R.id.order_status_reci);
        orderTotalTextView = findViewById(R.id.order_price_reci);
        downloadReceiptButton = findViewById(R.id.download_btn_reci);

        // Get order details from intent
        String productImageUrl = getIntent().getStringExtra("productImage");
        String getName = getIntent().getStringExtra("productName");
        String orderId = getIntent().getStringExtra("orderId");
        String orderDate = getIntent().getStringExtra("orderDate");
        String orderStatus = getIntent().getStringExtra("orderStatus");
        double orderTotal = getIntent().getDoubleExtra("orderTotal", 0.0);

        if (productImageUrl != null && !productImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(productImageUrl)
                    .into(productImageView);
        } else {
            productImageView.setImageResource(R.drawable.appetizers1); // Default image if URL is empty
        }

        productName.setText(getName);
        orderIdTextView.setText("Order ID: " + orderId);
        orderDateTextView.setText("Order Date: " + orderDate);
        orderStatusTextView.setText("Status: " + orderStatus);
        orderTotalTextView.setText(String.format("Total: Rs %.2f", orderTotal));

        // Handle download receipt button click
        downloadReceiptButton.setOnClickListener(v -> downloadReceipt(orderId, getName, orderDate, orderStatus, orderTotal));
    }

    private void downloadReceipt(String orderId, String productName, String orderDate, String orderStatus, double orderTotal) {
        // Create a file to save the PDF in the Downloads folder
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, "Receipt_" + orderId + ".pdf");

        try {
            // Initialize PDF writer and document
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add logo from drawable
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.greenify_food_logo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bitmapData = stream.toByteArray();

            Image logo = new Image(ImageDataFactory.create(bitmapData))
                    .setWidth(100)
                    .setAutoScale(true); // Automatically scale the height to maintain aspect ratio
            document.add(logo);

            // Add header with company details
            Paragraph companyName = new Paragraph("Greenify Organic Food")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GREEN);
            document.add(companyName);

            Paragraph companyAddress = new Paragraph("123 Organic Street, Green City, Earth")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(companyAddress);

            Paragraph companyContact = new Paragraph("Phone: +11 700 8654 | Email: greenify@biz.com")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(companyContact);

            // Add order receipt title
            Paragraph receiptTitle = new Paragraph("Order Receipt")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(receiptTitle);

            // Add order details table
            float[] columnWidths = {150f, 350f}; // Column widths for the table
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

            // Add rows to the table
            table.addCell(createCell("Order ID", true));
            table.addCell(createCell(orderId, false));

            table.addCell(createCell("Product Name", true));
            table.addCell(createCell(productName, false));

            table.addCell(createCell("Order Date", true));
            table.addCell(createCell(orderDate, false));

            table.addCell(createCell("Order Status", true));
            table.addCell(createCell(orderStatus, false));

            table.addCell(createCell("Total Amount", true));
            table.addCell(createCell("Rs " + String.format("%.2f", orderTotal), false));

            document.add(table);

            // Add thank you message
            Paragraph thankYouMessage = new Paragraph("Thank you for choosing Greenify Organic Food! We appreciate your business and hope to serve you again soon.")
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMarginTop(20)
                    .setFontColor(ColorConstants.GREEN);
            document.add(thankYouMessage);

            // Add footer
            Paragraph footer = new Paragraph("Visit us at: www.greenify.com")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20)
                    .setFontColor(ColorConstants.DARK_GRAY);
            document.add(footer);

            // Close the document
            document.close();

            // Notify the user
            CustomToast.showToast(OrderInvoiceActivity.this, "Receipt saved to " + file.getAbsolutePath(), true);
        } catch (IOException e) {
            e.printStackTrace();
            CustomToast.showToast(OrderInvoiceActivity.this, "Failed to generate receipt", false);
        }
    }

    private Cell createCell(String text, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(text));
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
        } else {
            cell.setTextAlignment(TextAlignment.LEFT);
        }
        return cell;
    }
}