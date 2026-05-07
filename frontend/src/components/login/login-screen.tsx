import { useEffect, useState } from "react";
import { FaUser, FaLock } from "react-icons/fa";
import "./Login.css";
import { getTenants } from "../../services/tenant-service";
import type Tenant from "../../models/tenant";
import { login } from "../../services/login-service";
import { useNavigate } from "react-router-dom";
import { Backdrop, CircularProgress, Typography } from "@mui/material";

const Login = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [tenants, setTenants] = useState([] as Tenant[]);
    const [tenantSelecionado, setTenantSelecionado] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        getTenants()
            .then(data => setTenants(data))
            .catch(error => console.error(error));
    }, [])

    const handleSubmit = (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();
        console.log("Dados de Login:", { username, password, tenantSelecionado });
        setIsLoading(true);
        login(username, password, tenantSelecionado)
            .then(_ => {
                setIsLoading(false);
                navigate("/dashboard");
            })
            .catch(_ => {
                alert("Ocorreu um erro durante a tentativa de login.");
                setIsLoading(false);
            });

    };

    const handleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
        setTenantSelecionado(event.target.value);
        console.log("Selecionado:", event.target.value);
    };;

    if (isLoading) {
        return <Backdrop
            sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
            open={isLoading}
        >
            <CircularProgress color="inherit" size={60} />

            <Typography variant="h6">
                Carregando pedidos...
            </Typography>
        </Backdrop>
    } else {
        return (
            <div className="container">
                <form onSubmit={handleSubmit}>
                    <h1>Acesse o sistema</h1>
                    <div className="input-field">
                        <input
                            type="text"
                            placeholder="E-mail"
                            required
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                        />
                        <FaUser className="icon" />
                    </div>
                    <div className="input-field">
                        <input
                            type="password"
                            placeholder="Senha"
                            required
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                        <FaLock className="icon" />
                    </div>
                    <div className="input-field">
                        <select id="dinamico" value={tenantSelecionado} onChange={handleChange} required>
                            <option value="">Selecione...</option>
                            {
                                tenants.map((item) => (
                                    <option key={item.code} value={item.code}>
                                        {item.name}
                                    </option>
                                ))}
                        </select>
                    </div>
                    <button type="submit">Login</button>

                </form>
            </div>
        );
    }

};

export default Login;