/**
 * Generated by orval v7.9.0 🍺
 * Do not edit manually.
 * Krisefikser API
 * API for the Krisefikser application
 * OpenAPI spec version: 1.0
 */
import { useMutation, useQuery } from '@tanstack/vue-query'
import type {
  DataTag,
  MutationFunction,
  QueryClient,
  QueryFunction,
  QueryKey,
  UseMutationOptions,
  UseMutationReturnType,
  UseQueryOptions,
  UseQueryReturnType,
} from '@tanstack/vue-query'

import { unref } from 'vue'
import type { MaybeRef } from 'vue'

import type { CreateUser, UserLocationRequest, UserResponse } from '.././model'

import { customInstance } from '../../axios'
import type { ErrorType, BodyType } from '../../axios'

type SecondParameter<T extends (...args: never) => unknown> = Parameters<T>[1]

/**
 * Updates an existing user's information
 * @summary Update user
 */
export const updateUser = (
  userId: MaybeRef<string>,
  createUser: MaybeRef<CreateUser>,
  options?: SecondParameter<typeof customInstance>,
) => {
  userId = unref(userId)
  createUser = unref(createUser)

  return customInstance<UserResponse>(
    {
      url: `http://localhost:8080/api/users/${userId}`,
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      data: createUser,
    },
    options,
  )
}

export const getUpdateUserMutationOptions = <
  TError = ErrorType<UserResponse>,
  TContext = unknown,
>(options?: {
  mutation?: UseMutationOptions<
    Awaited<ReturnType<typeof updateUser>>,
    TError,
    { userId: string; data: BodyType<CreateUser> },
    TContext
  >
  request?: SecondParameter<typeof customInstance>
}): UseMutationOptions<
  Awaited<ReturnType<typeof updateUser>>,
  TError,
  { userId: string; data: BodyType<CreateUser> },
  TContext
> => {
  const mutationKey = ['updateUser']
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined }

  const mutationFn: MutationFunction<
    Awaited<ReturnType<typeof updateUser>>,
    { userId: string; data: BodyType<CreateUser> }
  > = (props) => {
    const { userId, data } = props ?? {}

    return updateUser(userId, data, requestOptions)
  }

  return { mutationFn, ...mutationOptions }
}

export type UpdateUserMutationResult = NonNullable<Awaited<ReturnType<typeof updateUser>>>
export type UpdateUserMutationBody = BodyType<CreateUser>
export type UpdateUserMutationError = ErrorType<UserResponse>

/**
 * @summary Update user
 */
export const useUpdateUser = <TError = ErrorType<UserResponse>, TContext = unknown>(
  options?: {
    mutation?: UseMutationOptions<
      Awaited<ReturnType<typeof updateUser>>,
      TError,
      { userId: string; data: BodyType<CreateUser> },
      TContext
    >
    request?: SecondParameter<typeof customInstance>
  },
  queryClient?: QueryClient,
): UseMutationReturnType<
  Awaited<ReturnType<typeof updateUser>>,
  TError,
  { userId: string; data: BodyType<CreateUser> },
  TContext
> => {
  const mutationOptions = getUpdateUserMutationOptions(options)

  return useMutation(mutationOptions, queryClient)
}
/**
 * Deletes a user from the system
 * @summary Delete user
 */
export const deleteUser = (
  userId: MaybeRef<string>,
  options?: SecondParameter<typeof customInstance>,
) => {
  userId = unref(userId)

  return customInstance<void>(
    { url: `http://localhost:8080/api/users/${userId}`, method: 'DELETE' },
    options,
  )
}

export const getDeleteUserMutationOptions = <
  TError = ErrorType<void>,
  TContext = unknown,
>(options?: {
  mutation?: UseMutationOptions<
    Awaited<ReturnType<typeof deleteUser>>,
    TError,
    { userId: string },
    TContext
  >
  request?: SecondParameter<typeof customInstance>
}): UseMutationOptions<
  Awaited<ReturnType<typeof deleteUser>>,
  TError,
  { userId: string },
  TContext
> => {
  const mutationKey = ['deleteUser']
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined }

  const mutationFn: MutationFunction<Awaited<ReturnType<typeof deleteUser>>, { userId: string }> = (
    props,
  ) => {
    const { userId } = props ?? {}

    return deleteUser(userId, requestOptions)
  }

  return { mutationFn, ...mutationOptions }
}

export type DeleteUserMutationResult = NonNullable<Awaited<ReturnType<typeof deleteUser>>>

export type DeleteUserMutationError = ErrorType<void>

/**
 * @summary Delete user
 */
export const useDeleteUser = <TError = ErrorType<void>, TContext = unknown>(
  options?: {
    mutation?: UseMutationOptions<
      Awaited<ReturnType<typeof deleteUser>>,
      TError,
      { userId: string },
      TContext
    >
    request?: SecondParameter<typeof customInstance>
  },
  queryClient?: QueryClient,
): UseMutationReturnType<
  Awaited<ReturnType<typeof deleteUser>>,
  TError,
  { userId: string },
  TContext
> => {
  const mutationOptions = getDeleteUserMutationOptions(options)

  return useMutation(mutationOptions, queryClient)
}
/**
 * Updates the current user's location coordinates if location sharing is enabled
 * @summary Update user location
 */
export const updateCurrentUserLocation = (
  userLocationRequest: MaybeRef<UserLocationRequest>,
  options?: SecondParameter<typeof customInstance>,
) => {
  userLocationRequest = unref(userLocationRequest)

  return customInstance<UserResponse>(
    {
      url: `http://localhost:8080/api/users/location`,
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      data: userLocationRequest,
    },
    options,
  )
}

export const getUpdateCurrentUserLocationMutationOptions = <
  TError = ErrorType<UserResponse>,
  TContext = unknown,
>(options?: {
  mutation?: UseMutationOptions<
    Awaited<ReturnType<typeof updateCurrentUserLocation>>,
    TError,
    { data: BodyType<UserLocationRequest> },
    TContext
  >
  request?: SecondParameter<typeof customInstance>
}): UseMutationOptions<
  Awaited<ReturnType<typeof updateCurrentUserLocation>>,
  TError,
  { data: BodyType<UserLocationRequest> },
  TContext
> => {
  const mutationKey = ['updateCurrentUserLocation']
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined }

  const mutationFn: MutationFunction<
    Awaited<ReturnType<typeof updateCurrentUserLocation>>,
    { data: BodyType<UserLocationRequest> }
  > = (props) => {
    const { data } = props ?? {}

    return updateCurrentUserLocation(data, requestOptions)
  }

  return { mutationFn, ...mutationOptions }
}

export type UpdateCurrentUserLocationMutationResult = NonNullable<
  Awaited<ReturnType<typeof updateCurrentUserLocation>>
>
export type UpdateCurrentUserLocationMutationBody = BodyType<UserLocationRequest>
export type UpdateCurrentUserLocationMutationError = ErrorType<UserResponse>

/**
 * @summary Update user location
 */
export const useUpdateCurrentUserLocation = <TError = ErrorType<UserResponse>, TContext = unknown>(
  options?: {
    mutation?: UseMutationOptions<
      Awaited<ReturnType<typeof updateCurrentUserLocation>>,
      TError,
      { data: BodyType<UserLocationRequest> },
      TContext
    >
    request?: SecondParameter<typeof customInstance>
  },
  queryClient?: QueryClient,
): UseMutationReturnType<
  Awaited<ReturnType<typeof updateCurrentUserLocation>>,
  TError,
  { data: BodyType<UserLocationRequest> },
  TContext
> => {
  const mutationOptions = getUpdateCurrentUserLocationMutationOptions(options)

  return useMutation(mutationOptions, queryClient)
}
/**
 * Retrieves a list of all users in the system
 * @summary Get all users
 */
export const getAllUsers = (
  options?: SecondParameter<typeof customInstance>,
  signal?: AbortSignal,
) => {
  return customInstance<UserResponse[]>(
    { url: `http://localhost:8080/api/users`, method: 'GET', signal },
    options,
  )
}

export const getGetAllUsersQueryKey = () => {
  return ['http:', 'localhost:8080', 'api', 'users'] as const
}

export const getGetAllUsersQueryOptions = <
  TData = Awaited<ReturnType<typeof getAllUsers>>,
  TError = ErrorType<UserResponse[]>,
>(options?: {
  query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getAllUsers>>, TError, TData>>
  request?: SecondParameter<typeof customInstance>
}) => {
  const { query: queryOptions, request: requestOptions } = options ?? {}

  const queryKey = getGetAllUsersQueryKey()

  const queryFn: QueryFunction<Awaited<ReturnType<typeof getAllUsers>>> = ({ signal }) =>
    getAllUsers(requestOptions, signal)

  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<
    Awaited<ReturnType<typeof getAllUsers>>,
    TError,
    TData
  >
}

export type GetAllUsersQueryResult = NonNullable<Awaited<ReturnType<typeof getAllUsers>>>
export type GetAllUsersQueryError = ErrorType<UserResponse[]>

/**
 * @summary Get all users
 */

export function useGetAllUsers<
  TData = Awaited<ReturnType<typeof getAllUsers>>,
  TError = ErrorType<UserResponse[]>,
>(
  options?: {
    query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getAllUsers>>, TError, TData>>
    request?: SecondParameter<typeof customInstance>
  },
  queryClient?: QueryClient,
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getGetAllUsersQueryOptions(options)

  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & {
    queryKey: DataTag<QueryKey, TData, TError>
  }

  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>

  return query
}
