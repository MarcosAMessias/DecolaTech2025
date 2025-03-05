from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity

# Inicializa a aplicação Flask
app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///users.db'  # Banco de dados SQLite
app.config['JWT_SECRET_KEY'] = 'supersecretkey'  # Chave para JWT

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)
jwt = JWTManager(app)

# Modelo de usuário para persistência no banco de dados
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    password = db.Column(db.String(256), nullable=False)
    role = db.Column(db.String(20), nullable=False)

# Rota para registrar um novo usuário
@app.route('/users/register', methods=['POST'])
def register_user():
    data = request.get_json()
    hashed_password = bcrypt.generate_password_hash(data['password']).decode('utf-8')
    new_user = User(username=data['username'], password=hashed_password, role=data.get('role', 'user'))
    db.session.add(new_user)
    db.session.commit()
    return jsonify({"message": "Usuário registrado com sucesso!"}), 201

# Rota para autenticação de usuário e geração de token JWT
@app.route('/users/login', methods=['POST'])
def login():
    data = request.get_json()
    user = User.query.filter_by(username=data['username']).first()
    if user and bcrypt.check_password_hash(user.password, data['password']):
        access_token = create_access_token(identity=user.username)
        return jsonify({"access_token": access_token})
    return jsonify({"message": "Credenciais inválidas!"}), 401

# Rota protegida que lista todos os usuários (apenas autenticados podem acessar)
@app.route('/users/list', methods=['GET'])
@jwt_required()
def list_users():
    users = User.query.all()
    return jsonify([{ "id": user.id, "username": user.username, "role": user.role } for user in users])

# Inicializa o banco de dados
with app.app_context():
    db.create_all()

if __name__ == '__main__':
    app.run(debug=True)
