package it.univr.diabete.model;

public class Diabetologo {

    private String nome;
    private String cognome;
    private String email;
    private String password;
    private String numeroTelefono;
    private String sesso;
    private String laurea;

    public Diabetologo() {
    }

    public Diabetologo(String nome, String cognome, String email, String password, String numeroTelefono, String sesso, String laurea) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.numeroTelefono = numeroTelefono;
        this.sesso = sesso;
        this.laurea = laurea;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getNumeroTelefono() { return numeroTelefono; }
    public void setNumeroTelefono(String numeroTelefono) { this.numeroTelefono = numeroTelefono; }

    public String getSesso() { return sesso; }
    public void setSesso(String sesso) { this.sesso = sesso; }

    public String getLaurea() { return laurea;}
    public void setLaurea(String laurea) { this.laurea = laurea; }
}
