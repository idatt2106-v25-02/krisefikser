import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useVerifyAdminLogin } from '../api/generated/authentication/authentication';
import { useAuth } from '../contexts/AuthContext';
import { Box, CircularProgress, Typography } from '@mui/material';

export default function VerifyAdminLogin() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { setTokens } = useAuth();
  const token = searchParams.get('token');

  const { mutate: verifyAdminLogin, isError, isLoading } = useVerifyAdminLogin({
    mutation: {
      onSuccess: (data) => {
        // Store the tokens and redirect to admin dashboard
        setTokens(data.accessToken, data.refreshToken);
        navigate('/admin/dashboard');
      },
      onError: () => {
        // Redirect to login page on error
        navigate('/login');
      },
    },
  });

  useEffect(() => {
    if (token) {
      verifyAdminLogin({ params: { token } });
    } else {
      navigate('/login');
    }
  }, [token, verifyAdminLogin, navigate]);

  if (isLoading) {
    return (
      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        minHeight="100vh"
      >
        <CircularProgress />
        <Typography variant="h6" mt={2}>
          Verifying admin login...
        </Typography>
      </Box>
    );
  }

  if (isError) {
    return (
      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        minHeight="100vh"
      >
        <Typography variant="h6" color="error">
          Invalid or expired verification link
        </Typography>
        <Typography variant="body1" mt={2}>
          Please try logging in again
        </Typography>
      </Box>
    );
  }

  return null;
}
