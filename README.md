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
    
    Usuario "1"*--"1" Conta : possui
    Usuario "1"*--"N" Cartao : possui
    Usuario "1"*--"1" Feature : possui
    Usuario "1"*--> "N" Noticia : possui
