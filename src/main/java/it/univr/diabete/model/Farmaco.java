package it.univr.diabete.model;

public class Farmaco {

    private int id;
    private String nome;
    private String marca;

    public Farmaco() { }

    public Farmaco(int id, String nome, String marca) {
        this.id = id;
        this.nome = nome;
        this.marca = marca;
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

    @Override
    public String toString() {
        // comodo per le ComboBox
        return nome + " (" + marca + ")";
    }
}