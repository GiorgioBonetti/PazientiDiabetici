package it.univr.diabete.model;

public class Farmaco {

    private int id;
    private String nome;
    private String marca;
    private double dosaggio;

    public Farmaco() { }

    public Farmaco(int id, String nome, String marca, double dosaggio) {
        this.id = id;
        this.nome = nome;
        this.marca = marca;
        this.dosaggio = dosaggio;
    }

    // getter & setter
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMarca() {
        return marca;
    }
    public void setMarca(String marca) {
        this.marca = marca;
    }

    public double getDosaggio() { return dosaggio; }
    public void setDosaggio(double dosaggio) { this.dosaggio = dosaggio; }

    @Override
    public String toString() {
        // comodo per le ComboBox
        return nome + " (" + marca + ")";
    }
}