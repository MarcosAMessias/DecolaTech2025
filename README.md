# DecolaTech2025
 Anotações dos códigos para estudo
```mermaid
classDiagram
    class Usuario {
        +string name
    }
    
    class Conta {
        +string number
        +string agency
        +float balance
        +float limit
    }
    
    class Cartao {
        +string number
        +float limit
    }
    
    class Feature {
        +string icon
        +string description
    }
    
    class Noticia {
        +string icon
        +string description
    }
    
    Usuario *--> Conta : possui
    Usuario *--> Cartao : possui
    Usuario *--> "0..*" Feature : possui
    Usuario *--> "0..*" Noticia : possui
