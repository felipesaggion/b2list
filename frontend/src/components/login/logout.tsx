import { useNavigate } from "react-router-dom";
import { logout } from "../../services/login-service";

const Logout = () => {
    const navigate = useNavigate();
    setTimeout(() => {
        logout();
        navigate("/");
    }, 500);
    return <>Saindo da aplicação...</>;
}

export default Logout;