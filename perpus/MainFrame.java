package perpus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import dao.BukuDAO;
import dao.BukuDAOImpl;
import dao.PeminjamanDAO;
import model.Anggota;
import model.Buku;

public class MainFrame extends JFrame {
    private BukuDAO bukuDAO;
    private PeminjamanDAO peminjamanDAO;
    
    // --- Komponen Tab Buku ---
    private JTable tableBuku;
    private DefaultTableModel tableModelBuku;
    private JTextField txtJudul, txtPenulis, txtStok;
    private JComboBox<String> comboJenis;
    private JButton btnSimpanBuku, btnHapusBuku, btnBatalBuku;
    private int idBukuTerpilih = 0; 

    // --- Komponen Tab Peminjaman ---
    private JComboBox<Anggota> comboAnggota;
    private JComboBox<Buku> comboBukuPinjam;

    // --- Komponen Tab Pengembalian ---
    private JTable tableKembali;
    private DefaultTableModel tableModelKembali;
    private JButton btnProsesKembali;
    private int idPinjamTerpilih = 0;
    private int idBukuKembali = 0; 

    public MainFrame() {
        // Inisialisasi DAO
        bukuDAO = new BukuDAOImpl();
        peminjamanDAO = new PeminjamanDAO();

        setTitle("Sistem Perpustakaan PBO");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manajemen Buku", createPanelBuku());
        tabbedPane.addTab("Transaksi Peminjaman", createPanelPeminjaman());
        tabbedPane.addTab("Transaksi Pengembalian", createPanelPengembalian());
        tabbedPane.addTab("Laporan Riwayat", createPanelLaporan());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Load data awal
        loadDataBuku();
        loadDataPeminjamanAktif();
        refreshDropdownData();
    }

    private JPanel createPanelBuku() {
        JPanel panel = new JPanel(new BorderLayout());

        // 1. Tabel (Bagian Tengah)
        String[] colNames = {"ID", "Jenis Buku", "Judul", "Penulis", "Stok"};
        tableModelBuku = new DefaultTableModel(colNames, 0);
        tableBuku = new JTable(tableModelBuku);
        tableBuku.setDefaultEditor(Object.class, null);

        // tambahkan JScrollPane lalu sembunyikan kolom ID di view (tetap ada di model)
        JScrollPane sp = new JScrollPane(tableBuku);
        panel.add(sp, BorderLayout.CENTER);

        if (tableBuku.getColumnModel().getColumnCount() > 0) {
            // removeColumn bekerja pada view; model tetap menyimpan kolom ID di index 0
            tableBuku.removeColumn(tableBuku.getColumnModel().getColumn(0));
        }

        // Event Klik Tabel (gunakan convertRowIndexToModel karena kolom di-hide)
        tableBuku.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int viewRow = tableBuku.getSelectedRow();
                if (viewRow != -1) {
                    int modelRow = tableBuku.convertRowIndexToModel(viewRow);
                    idBukuTerpilih = Integer.parseInt(tableModelBuku.getValueAt(modelRow, 0).toString());
                    comboJenis.setSelectedItem(tableModelBuku.getValueAt(modelRow, 1).toString());
                    txtJudul.setText(tableModelBuku.getValueAt(modelRow, 2).toString());
                    txtPenulis.setText(tableModelBuku.getValueAt(modelRow, 3).toString());
                    txtStok.setText(tableModelBuku.getValueAt(modelRow, 4).toString());
                    
                    btnSimpanBuku.setText("Update Data");
                    btnHapusBuku.setEnabled(true);
                }
            }
        });

        // 2. Panel Bawah (Gabungan Form & Tombol)
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Form Input
        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Form Input Buku"));
        
        String[] jenis = {"Edukasi", "Teknologi", "Sejarah", "Motivasi", "Novel", "Komik", "Ensiklopedia", "Lainnya"};
        comboJenis = new JComboBox<>(jenis);
        txtJudul = new JTextField();
        txtPenulis = new JTextField();
        txtStok = new JTextField();
        
        form.add(new JLabel("Jenis Buku:")); form.add(comboJenis);
        form.add(new JLabel("Judul:")); form.add(txtJudul);
        form.add(new JLabel("Penulis:")); form.add(txtPenulis);
        form.add(new JLabel("Stok:")); form.add(txtStok);

        // Panel Tombol
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSimpanBuku = new JButton("Simpan");
        btnHapusBuku = new JButton("Hapus"); 
        btnBatalBuku = new JButton("Batal / Reset");

        // Styling Tombol
        btnSimpanBuku.setBackground(new Color(50, 200, 50)); btnSimpanBuku.setForeground(Color.WHITE);
        btnHapusBuku.setBackground(new Color(220, 50, 50)); btnHapusBuku.setForeground(Color.WHITE);
        btnHapusBuku.setEnabled(false);

        btnSimpanBuku.addActionListener(e -> aksiSimpanBuku());
        btnHapusBuku.addActionListener(e -> aksiHapusBuku());
        btnBatalBuku.addActionListener(e -> resetFormBuku());

        btnPanel.add(btnSimpanBuku);
        btnPanel.add(btnHapusBuku);
        btnPanel.add(btnBatalBuku);

        // Masukkan Form dan Tombol ke BottomPanel
        bottomPanel.add(form, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        // Masukkan BottomPanel ke Panel Utama
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    // =================================================
    //  TAB 2: PEMINJAMAN
    // =================================================
    private JPanel createPanelPeminjaman() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        comboAnggota = new JComboBox<>();
        comboBukuPinjam = new JComboBox<>();
        
        // Custom Renderer
        comboBukuPinjam.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Buku) {
                    Buku b = (Buku) value;
                    setText(b.getJudul() + " - Stok: " + b.getStok());
                    if (b.getStok() <= 0) setForeground(Color.RED); else setForeground(Color.BLACK);
                }
                return this;
            }
        });

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Pilih Anggota:"), gbc);
        gbc.gridx=1; panel.add(comboAnggota, gbc);
        
        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Pilih Buku:"), gbc);
        gbc.gridx=1; panel.add(comboBukuPinjam, gbc);

        JButton btnPinjam = new JButton("PROSES PEMINJAMAN");
        btnPinjam.setBackground(new Color(100, 200, 255));
        
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2; 
        panel.add(btnPinjam, gbc);

        btnPinjam.addActionListener(e -> {
            Anggota a = (Anggota) comboAnggota.getSelectedItem();
            Buku b = (Buku) comboBukuPinjam.getSelectedItem();
            if(a != null && b != null) {
                if (b.getStok() > 0) {
                    if(peminjamanDAO.prosesPeminjaman(a, b)) {
                        JOptionPane.showMessageDialog(this, "Peminjaman Berhasil!");
                        loadDataBuku(); 
                        loadDataPeminjamanAktif(); 
                        refreshDropdownData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal memproses transaksi.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Stok Buku Habis!", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        return panel;
    }

    // =================================================
    //  TAB 3: PENGEMBALIAN
    // =================================================
    private JPanel createPanelPengembalian() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Daftar Buku Yang Sedang Dipinjam"));

        // Tabel
        String[] colNames = {"ID Pinjam", "Peminjam", "Judul Buku", "Tgl Pinjam", "Jatuh Tempo", "ID Buku (Hidden)"};
        tableModelKembali = new DefaultTableModel(colNames, 0);
        tableKembali = new JTable(tableModelKembali);
        panel.add(new JScrollPane(tableKembali), BorderLayout.CENTER);

        // Tombol
        btnProsesKembali = new JButton("KEMBALIKAN BUKU & UPDATE STOK");
        btnProsesKembali.setBackground(new Color(255, 100, 100));
        btnProsesKembali.setForeground(Color.WHITE);
        btnProsesKembali.setFont(new Font("Arial", Font.BOLD, 14));
        btnProsesKembali.setEnabled(false);

        panel.add(btnProsesKembali, BorderLayout.SOUTH);

        // Event Klik Tabel
        tableKembali.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableKembali.getSelectedRow();
                if (row != -1) {
                    idPinjamTerpilih = Integer.parseInt(tableModelKembali.getValueAt(row, 0).toString());
                    idBukuKembali = Integer.parseInt(tableModelKembali.getValueAt(row, 5).toString());
                    btnProsesKembali.setEnabled(true);
                    btnProsesKembali.setText("Kembalikan Buku: " + tableModelKembali.getValueAt(row, 2).toString());
                }
            }
        });

        // Aksi Tombol
        btnProsesKembali.addActionListener(e -> {
            if (idPinjamTerpilih > 0) {
                String strDenda = JOptionPane.showInputDialog(this, "Masukkan Denda (jika ada, tulis 0):", "0");
                if (strDenda != null) {
                    try {
                        int denda = Integer.parseInt(strDenda);
                        boolean sukses = peminjamanDAO.kembalikanBuku(idPinjamTerpilih, idBukuKembali, denda);
                        
                        if (sukses) {
                            JOptionPane.showMessageDialog(this, "Buku Dikembalikan! Stok bertambah.");
                            loadDataPeminjamanAktif();
                            loadDataBuku();
                            refreshDropdownData();
                            btnProsesKembali.setEnabled(false);
                            btnProsesKembali.setText("KEMBALIKAN BUKU & UPDATE STOK");
                        } else {
                            JOptionPane.showMessageDialog(this, "Gagal mengembalikan.");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Denda harus angka!");
                    }
                }
            }
        });

        return panel;
    }

    // =================================================
    //  LOGIKA PENDUKUNG
    // =================================================
    private void loadDataBuku() {
        tableModelBuku.setRowCount(0);
        for (Buku b : bukuDAO.getAllBuku()) {
            tableModelBuku.addRow(new Object[]{
                b.getIdBuku(), b.getJenisBuku(), b.getJudul(), b.getPenulis(), b.getStok()
            });
        }
    }

    private void loadDataPeminjamanAktif() {
        tableModelKembali.setRowCount(0);
        List<Object[]> list = peminjamanDAO.getPeminjamanAktif();
        for (Object[] row : list) {
            tableModelKembali.addRow(row);
        }
    }

    private void aksiSimpanBuku() {
        try {
            String judul = txtJudul.getText();
            String penulis = txtPenulis.getText();
            String stokStr = txtStok.getText();

            if(judul.isEmpty() || penulis.isEmpty() || stokStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua data wajib diisi!");
                return;
            }

            Buku buku = new Buku(idBukuTerpilih, (String)comboJenis.getSelectedItem(), judul, penulis, Integer.parseInt(stokStr));
            
            if (idBukuTerpilih == 0) {
                bukuDAO.simpan(buku);
                JOptionPane.showMessageDialog(this, "Data tersimpan!");
            } else {
                bukuDAO.update(buku);
                JOptionPane.showMessageDialog(this, "Data diupdate!");
            }
            
            loadDataBuku(); 
            refreshDropdownData(); 
            resetFormBuku();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Stok harus angka!");
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); 
        }
    }

    private void aksiHapusBuku() {
        if (idBukuTerpilih > 0) {
            int confirm = JOptionPane.showConfirmDialog(this, "Hapus buku ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                bukuDAO.hapus(idBukuTerpilih);
                loadDataBuku(); 
                refreshDropdownData(); 
                resetFormBuku();
                JOptionPane.showMessageDialog(this, "Data dihapus.");
            }
        }
    }

    private void resetFormBuku() {
        idBukuTerpilih = 0; 
        txtJudul.setText(""); 
        txtPenulis.setText(""); 
        txtStok.setText("");
        comboJenis.setSelectedIndex(0);
        
        btnSimpanBuku.setText("Simpan"); 
        btnHapusBuku.setEnabled(false); 
        tableBuku.clearSelection();
    }

    private void refreshDropdownData() {
        comboAnggota.removeAllItems();
        for (Anggota a : peminjamanDAO.getAllAnggota()) comboAnggota.addItem(a);

        comboBukuPinjam.removeAllItems();
        for (Buku b : bukuDAO.getAllBuku()) comboBukuPinjam.addItem(b);
    }

    public static void main(String[] args) {
        //try { 
        //    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        //} catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    // =================================================
    //  TAB 4: LAPORAN RIWAYAT (Pencatatan Utuh)
    // =================================================
    private JPanel createPanelLaporan() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Riwayat Sirkulasi Perpustakaan"));

        // Tabel Riwayat
        String[] colNames = {"ID", "Peminjam", "Buku", "Tgl Pinjam", "Jatuh Tempo", "Tgl Kembali", "Denda", "Status"};
        DefaultTableModel modelLaporan = new DefaultTableModel(colNames, 0);
        JTable tableLaporan = new JTable(modelLaporan);
        
        // Isi data menggunakan method baru tadi
        List<Object[]> list = peminjamanDAO.getRiwayatLengkap();
        for (Object[] row : list) {
            modelLaporan.addRow(row);
        }

        // Tombol Refresh
        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.addActionListener(e -> {
            modelLaporan.setRowCount(0);
            List<Object[]> dataBaru = peminjamanDAO.getRiwayatLengkap();
            for (Object[] row : dataBaru) {
                modelLaporan.addRow(row);
            }
        });

        panel.add(new JScrollPane(tableLaporan), BorderLayout.CENTER);
        panel.add(btnRefresh, BorderLayout.SOUTH);
        
        return panel;
    }
}