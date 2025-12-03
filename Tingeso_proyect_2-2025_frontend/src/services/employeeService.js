import httpClient from './http-common';

const create = (data, password) => {
    return httpClient.post('api/employees', data, {
      params: {
        password: password
      }
    });
}

const getAll = () => {
  return httpClient.get('/api/employees/');
}

const getByRun = run => {
  return httpClient.get(`/api/employees/${run}`);
}

const getByIsAdmin = () => {
    return httpClient.get('/api/employees/isAdmin');
}

const update = (run, data) => {
  return httpClient.put(`/api/employees/employee/${run}`, data);
}

const removeEmployee = employeeRun => {
  return httpClient.delete(`/api/employees/delete/${employeeRun}`);
}

const removeClient = clientRun => {
  return httpClient.delete(`/api/employees/client/delete/${clientRun}`);
}

export default { create, getAll, getByRun, getByIsAdmin, update, removeEmployee, removeClient }