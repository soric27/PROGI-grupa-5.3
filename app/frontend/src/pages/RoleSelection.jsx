import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

function RoleSelection() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const tokenFromQuery = searchParams.get("token");

  useEffect(() => {
    if (tokenFromQuery) {
      sessionStorage.setItem("auth_token", tokenFromQuery);
      window.location.replace("/");
      return;
    }
    navigate("/");
  }, [tokenFromQuery, navigate]);

  return null;
}

export default RoleSelection;
